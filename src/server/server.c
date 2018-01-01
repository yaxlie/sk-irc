#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <pthread.h>
#include "stack.h"

#define SERVER_PORT 12345
#define MAX_USERS 100
#define CLIENT_PORT 2000
#define QUEUE_SIZE 5


int client_port = CLIENT_PORT;
//struktura pokoju
struct Room
{
    int id;
    char name[20];
    char password[20];
    int limit; //limit uzytkownikow
    int users;  //liczba obecnych w pokoju uzytkownikow
};

//struktura wiadomosci
struct Message
{
    char text[240];
    char sender[20];
    char receiver[20];
    char date[40];
};

//struktura uzytkownikow
struct User
{
    int port;
    char name[20];
};

struct data2send
{
    struct Room listaPokojow[10];
    struct User users[100];
};

//struktura zawierajÄca dane, ktĂłre zostanÄ przekazane do wÄtku
struct thread_data_t
{
    //socket prosby o przydzielenie portu
    int sfd;
    
    //users lobby sockets
    int uls[100];
    struct data2send data;
};

struct data_lobby
{
    struct thread_data_t main_data;
    int c_id;
};


struct thread_data_t *t_data_main; 


int assignCPort(struct User u[])
{
    int i;
    for(i=0;i<MAX_USERS;i++)
    {
        if(u[i].port == 0)
            break;
    }
    return i;
}
    

//funkcja opisujÄcÄ zachowanie wÄtku - musi przyjmowaÄ argument typu (void *) i zwracaÄ (void *)
void *SendLobbyBehavior(void *arg)
{
    pthread_detach(pthread_self());
    int id = (int*) arg;
     printf("%d\n", id);
    
    int connection_socket_descriptor = accept((*t_data_main).uls[id], NULL, NULL);
    if (connection_socket_descriptor < 0)
    {
        printf(": Błąd przy próbie utworzenia gniazda dla połączenia.\n");
        exit(1);
    }
        /*struct Message m;
        strncpy(m.text, msg, sizeof(m.text));
        strncpy(m.sender, "server", sizeof(m.sender));
        strncpy(m.receiver, "client", sizeof(m.receiver));
        strncpy(m.date, "10-10-2010", sizeof(m.date));*/
    write(connection_socket_descriptor, &(*t_data_main).data, sizeof(struct data2send));
    printf("wyslano lobby do klienta :  \n");
    close(connection_socket_descriptor);
    pthread_exit(NULL);
}

//funkcja obsĹugujÄca poĹÄczenie z nowym klientem
void handleConnection(int connection_socket_descriptor) {
    //wynik funkcji tworzÄcej wÄtek
    int create_result = 0;

    //uchwyt na wÄtek
    pthread_t thread1[100];


    //dane, ktĂłre zostanÄ przekazane do wÄtku
    struct thread_data_t *th_data = (struct thread_data_t*)t_data_main;
    (*th_data).sfd = connection_socket_descriptor;
    struct data_lobby d_lobby;
    d_lobby.main_data = (*th_data);

    char msg[20];      
    if(read((*th_data).sfd,msg,sizeof(msg))){
        int id = assignCPort((*th_data).data.users);
        int port = id + CLIENT_PORT;
        (*th_data).data.users[id].port = port;
        strncpy((*th_data).data.users[id].name, msg, sizeof(msg));
        
        int conv_port = htonl(port);
        write((*th_data).sfd, &conv_port, sizeof(conv_port));
        printf("client: %s chce uzyskać port \n",msg);
        
        //send lobby data
        int i;
        for (i=0; i<MAX_USERS; i++)
        {
            //printf("%d ", (*th_data).data.users[i].port);
            if((*th_data).data.users[i].port != 0)
            {
                
                create_result = pthread_create(&thread1[i], NULL, SendLobbyBehavior, (void*)i);
                 //printf("nowy watek\n");
                if (create_result){
                printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
                exit(-1);
                }
            }
        }
        //printf("\n");
    }
}

int createSocket(int port)
{
   int server_socket_descriptor;
   int connection_socket_descriptor;
   int bind_result;
   int listen_result;
   char reuse_addr_val = 1;
   struct sockaddr_in server_address;

   //inicjalizacja gniazda serwera
   memset(&server_address, 0, sizeof(struct sockaddr));
   server_address.sin_family = AF_INET;
   server_address.sin_addr.s_addr = htonl(INADDR_ANY);
   server_address.sin_port = htons(port);

   server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);
   if (server_socket_descriptor < 0)
   {
       fprintf(stderr, ": Błąd przy próbie utworzenia gniazda..\n");
       exit(1);
   }
   setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&reuse_addr_val, sizeof(reuse_addr_val));

   bind_result = bind(server_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
   if (bind_result < 0)
   {
       fprintf(stderr, ": Błąd przy próbie dowiązania adresu IP i numeru portu do gniazda.\n");
       exit(1);
   }

   listen_result = listen(server_socket_descriptor, QUEUE_SIZE);
   if (listen_result < 0) {
       fprintf(stderr, ": Błąd przy próbie ustawienia wielkości kolejki.\n");
       exit(1);
   }
   return server_socket_descriptor;
}

int main(int argc, char* argv[])
{
    t_data_main = malloc(sizeof(struct thread_data_t));
    int connection_socket_descriptor;
    int server_socket_descriptor;
    server_socket_descriptor = createSocket(SERVER_PORT);
    
    //tworzenie socketow uls
    int i;
    for(i=0; i<MAX_USERS; i++)
    {
        (*t_data_main).uls[i] = createSocket(CLIENT_PORT + i);
    }
    
   while(1)
   {
        connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);
       if (connection_socket_descriptor < 0)
       {
           fprintf(stderr, "%s: Błąd przy próbie utworzenia gniazda dla połączenia.\n", argv[0]);
           exit(1);
       }

       handleConnection(connection_socket_descriptor);
   }

   close(connection_socket_descriptor);
   return(0);
   
}

