#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>

static sem_t rd[5], wrt[5];
static int readcnt[5];

void *server(void *tskt) {

	int skt = *(int *) tskt;
	char buffer[1024] = { 0 };
	char op[10], c[2], *filename;
	filename = (char *) malloc(20 * sizeof(char));
	char msg[100];
	int j;

	memset(c, 0, 1024);
	recv(skt, c, 1024, 0);
	j = atoi(c);

	switch (j) {
	case 1:
		sprintf(filename, "%s.txt", "Dir1/File1");
		break;
	case 2:
		sprintf(filename, "%s.txt", "Dir1/File2");
		break;
	case 3:
		sprintf(filename, "%s.txt", "Dir2/File1");
		break;
	case 4:
		sprintf(filename, "%s.txt", "Dir2/File2");
		break;
	}

	memset(op, 0, 1024);
	recv(skt, op, 1024, 0);
	printf("%s\n", op);

	if (strcmp(op, "read") == 0) {

		sem_wait(&rd[j]);
		readcnt[j]++;
		if (readcnt[j] == 1)
			sem_wait(&wrt[j]);
		sem_post(&rd[j]);

		strcpy(msg, "Reading Mode");
		send(skt, msg, strlen(msg), 0);

		FILE *fp;
		fp = fopen(filename, "r");
		memset(buffer, 0, 1024);
		char ch;
		int i = 0;
		while ((ch = fgetc(fp)) != EOF) {
			buffer[i++] = ch;
			buffer[i] = '\0';
		}
		fclose(fp);
		printf("File Contents Sent\n");
		send(skt, buffer, strlen(buffer), 0);

		memset(buffer, 0, 1024);
		recv(skt, buffer, 1024, 0);
		printf("%s\n", buffer);

		sem_wait(&rd[j]);
		readcnt[j]--;
		if (readcnt[j] == 0)
			sem_post(&wrt[j]);
		sem_post(&rd[j]);
		printf("End Read\n");

		strcpy(msg, "Close");
		send(skt, msg, strlen(msg), 0);

	} else if (strcmp(op, "write") == 0) {

		sem_wait(&wrt[j]);

		strcpy(msg, "Writing Mode");
		send(skt, msg, strlen(msg), 0);

		memset(buffer, 0, 1024);
		recv(skt, buffer, 1024, 0);
		printf("%s\n", buffer);
		FILE *fp;
		fp = fopen(filename, "w");
		fprintf(fp, "%s", buffer);
		fclose(fp);

		sem_post(&wrt[j]);
		printf("End Write\n");

	} else {

	}

	return 0;
}

void init() {
	int i;
	for (i = 0; i < 5; i++) {
		readcnt[i] = 0;
		sem_init(&wrt[i], 1, 1);
		sem_init(&rd[i], 1, 1);

	}
}

int main() {

	int server_fd, skt, readmsg;
	struct sockaddr_in address;
	int opt = 1;
	int addrlen = sizeof(address);
	pthread_t t;
	init();

	server_fd = socket(AF_INET, SOCK_STREAM, 0);

	setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt,
			sizeof(opt));
	address.sin_family = AF_INET;
	address.sin_addr.s_addr = INADDR_ANY;
	address.sin_port = htons(5000);

	bind(server_fd, (struct sockaddr *) &address, sizeof(address));
	listen(server_fd, 3);

	while (1) {
		skt = accept(server_fd, (struct sockaddr *) &address,
				(socklen_t*) &addrlen);
		pthread_create(&t, NULL, server, (void *) &skt);
	}

	return 1;
}
