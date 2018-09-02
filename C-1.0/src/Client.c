#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string.h>

int main(int argc, char const *argv[]) {
	struct sockaddr_in address;
	int sock = 0, readmsg;
	struct sockaddr_in serv_addr;
	char wrtmsg[10];
	char buffer[1024] = { 0 };
	sock = socket(AF_INET, SOCK_STREAM, 0);

	char msg[100];
	int d, f;
	char c[2];

	memset(&serv_addr, '0', sizeof(serv_addr));

	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(5000);

	inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr);

	connect(sock, (struct sockaddr *) &serv_addr, sizeof(serv_addr));

	a: printf("Select Directory\n");
	printf("1. Dir1 \t 2. Dir2\n");
	scanf("%d", &d);
	printf("Select File\n");
	printf("1. File1 \t 2. File2\n");
	scanf("%d", &f);

	switch (d) {
	case 1:
		switch (f) {
		case 1:
			c[0] = '1';
			c[1] = '\0';
			break;
		case 2:
			c[0] = '2';
			c[1] = '\0';
			break;
		default:
			printf("Invalid Entry");
			goto a;
			break;
		}
		break;

	case 2:
		switch (f) {
		case 1:
			c[0] = '3';
			c[1] = '\0';
			break;
		case 2:
			c[0] = '4';
			c[1] = '\0';
			break;

		default:
			printf("Invalid Entry");
			goto a;
			break;
		}
		break;

	default:
		printf("Invalid Entry");
		goto a;
		break;
	}

	send(sock, c, strlen(c), 0);
	c: printf("Enter Operation (read/write)\n");
	scanf("%s", wrtmsg);
	send(sock, wrtmsg, strlen(wrtmsg), 0);

	if (strcmp(wrtmsg, "read") == 0) {

		printf("Waiting for Server\n");

		memset(buffer, 0, 1024);
		recv(sock, buffer, 1024, 0);
		printf("%s\n", buffer);

		printf("The contents are:\n");
		memset(buffer, 0, 1024);
		recv(sock, buffer, 1024, 0);
		printf("%s\n", buffer);

		b: printf("Type 'exit' to Exit\n");
		scanf("%s", msg);
		if (strcmp(msg, "exit") != 0)
			goto b;
		send(sock, msg, strlen(msg), 0);

		memset(buffer, 0, 1024);
		recv(sock, buffer, 1024, 0);
		printf("%s\n", buffer);

	} else if (strcmp(wrtmsg, "write") == 0) {

		printf("Waiting for Server\n");

		memset(buffer, 0, 1024);
		recv(sock, buffer, 1024, 0);
		printf("%s\n", buffer);

		memset(buffer, 0, 1024);
		memset(msg, 0, 100);
		printf("Enter text('.' to send)\n");
		getchar();
		scanf("%[^\n]", msg);
		while (strcmp(msg, ".") != 0) {
			strcat(buffer, msg);
			strcat(buffer, "\n");
			memset(msg, 0, 100);
			getchar();
			scanf("%[^\n]", msg);
		}

		send(sock, buffer, strlen(buffer), 0);

	} else {
		goto c;
	}

	return 0;
}
