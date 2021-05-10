#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/ip.h>
#include <sys/wait.h>

#define PORT 6060


int exec_command(int sock, char* buf) {

char command[85];
char* val1 = 0;
close(STDOUT_FILENO);
close(STDERR_FILENO);
dup2(sock, STDOUT_FILENO);
dup2(sock, STDERR_FILENO);
val1 = strtok(buf, "\n");
sprintf(command, "cat ./notes | grep -i %s", val1);
system(command);
return 0;
}

void main()
{
struct sockaddr_in server;
struct sockaddr_in client;
int clientLen;
int sock, newsock;
char buf[1500];
pid_t pid, current = getpid();
int ret_val;

sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

if (sock < 0) {
perror("Error opening socket");
exit(1);
}
memset((char*)&server, 0, sizeof(server));
server.sin_family = AF_INET;
server.sin_addr.s_addr = htonl(INADDR_ANY);
server.sin_port = htons(PORT);

ret_val = bind(sock, (struct sockaddr*)&server, sizeof(server));
if (ret_val < 0) {
perror("ERROR on binding");
close(sock);
exit(1);
}


while (1) {

listen(sock, 5);
clientLen = sizeof(client);
newsock = accept(sock, (struct sockaddr*)&client, &clientLen);
if (newsock < 0) {
perror("Error on accept");
exit(1);
}
bzero(buf, 1500);
recvfrom(newsock, buf, 1500 - 1, 0, (struct sockaddr*)&client, &clientLen);
printf("the buf: %s||\n", buf);
exec_command(newsock, buf);
//printf("the end\n");
close(newsock);

}
close(sock);
}
