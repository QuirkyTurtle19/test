#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/ip.h>
#include <sys/wait.h>

#define PORT 6060

int exec_command(int sock, char *buf) {

   char command[88];
   char *command_p=command;
   char *val0=0;
   char *val1=0;
   int status=10;
   close(STDOUT_FILENO);
   close(STDERR_FILENO);
   dup2(sock, STDOUT_FILENO);
   dup2(sock, STDERR_FILENO );
   sprintf(command_p, "%s",  buf);
   val1= strtok(command, "\n");
   char * argv_list[] = {"/bin/grep", "-i", val1, "notes", NULL};
   printf("You have provided: \n");
   printf(command);
pid_t id = fork();
if (id == -1) exit(1);
if (id > 0)
{
   waitpid(id, &status, 0);
   printf("\nTEST\n");
   return 0;
} else {    
if(execve("/bin/grep", argv_list, NULL)==0){
return -1;    
};
}
}

void main()
{
    struct sockaddr_in server;
    struct sockaddr_in client;
    int clientLen;
    int sock,newsock;
    char buf[1500];
    pid_t pid,current = getpid();
    int ret_val;

    sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

    if (sock < 0) {
    perror("Error opening socket");
    exit(1);
    }
    memset((char *) &server, 0, sizeof(server));
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = htonl(INADDR_ANY);
    server.sin_port = htons(PORT);

    ret_val = bind(sock, (struct sockaddr *) &server, sizeof(server));
    if (ret_val < 0) {
        perror("ERROR on binding");
close(sock);
        exit(1);
    }


    while (1) {

    listen(sock, 5);
clientLen = sizeof(client);
        newsock = accept(sock, (struct sockaddr *) &client, &clientLen);
        if (newsock < 0) {
        perror("Error on accept");
        exit(1);
        }
bzero(buf, 1500);
recvfrom(newsock, buf, 1500-1, 0, (struct sockaddr *) &client, &clientLen);
printf("the buf: %s||\n",buf);
exec_command(newsock, buf);
//printf("the end\n");
close(newsock);
             
    }
    close(sock);
}
