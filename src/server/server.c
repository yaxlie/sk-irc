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

#define SERVER_PORT 12345
#define QUEUE_SIZE 5

//struktura pokoju
struct Room
{
    int id;
    char name[20];
    char password[20];
    int port;
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
    int id;
    char name[20];
    char password[20];
};


//struktura zawierajÄca dane, ktĂłre zostanÄ przekazane do wÄtku
struct thread_data_t
{
    int sfd;
};

    struct Room listaPokojow[20];

//funkcja opisujÄcÄ zachowanie wÄtku - musi przyjmowaÄ argument typu (void *) i zwracaÄ (void *)
void *ThreadBehavior(void *t_data)
{
    int i = 0;
	//inicjalizajca pokojow
    while(i < 20){
	listaPokojow[i].id = i;
	listaPokojow[i].port = 7500 + (i * 10);
	strcpy(listaPokojow[i].name, "nazwa");
	listaPokojow[i].limit = 10;
	listaPokojow[i].users = 0;
     }
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;
    //dostÄp do pĂłl struktury: (*th_data).pole
    char msg[240];
    while(1){
        fgets(msg, sizeof(msg), stdin);
        
        if (msg[strlen(msg) - 1] == '\n') {
            msg[strlen(msg) - 1] == '\0';
            }
        
        struct Message m;
        strncpy(m.text, msg, sizeof(m.text));
        strncpy(m.sender, "server", sizeof(m.sender));
        strncpy(m.receiver, "client", sizeof(m.receiver));
        strncpy(m.date, "10-10-2010", sizeof(m.date));
        
        write( (*th_data).sfd, &m, sizeof(struct Message));
    }
    pthread_exit(NULL);
}

//funkcja obsĹugujÄca poĹÄczenie z nowym klientem
void handleConnection(int connection_socket_descriptor) {
    //wynik funkcji tworzÄcej wÄtek
    int create_result = 0;

    //uchwyt na wÄtek
    pthread_t thread1;


    //dane, ktĂłre zostanÄ przekazane do wÄtku
    struct thread_data_t *th_data = malloc(sizeof(struct thread_data_t));
    (*th_data).sfd = connection_socket_descriptor;

    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)th_data);
    if (create_result){
       printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
       exit(-1);
    }

    char msg[128];
   
	write( (*th_data).sfd, &listaPokojow, sizeof(struct Room)*20);
	while(1){        
	read((*th_data).sfd,msg,sizeof(msg));
        printf("client: %s chce uzyskać port \n",msg);
        //TODO wyslac przydzielony port dla clienta, ktory ch
        break;
    }
}

int main(int argc, char* argv[])
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
   server_address.sin_port = htons(SERVER_PORT);

   server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);
   if (server_socket_descriptor < 0)
   {
       fprintf(stderr, "%s: Błąd przy próbie utworzenia gniazda..\n", argv[0]);
       exit(1);
   }
   setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&reuse_addr_val, sizeof(reuse_addr_val));

   bind_result = bind(server_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
   if (bind_result < 0)
   {
       fprintf(stderr, "%s: Błąd przy próbie dowiązania adresu IP i numeru portu do gniazda.\n", argv[0]);
       exit(1);
   }

   listen_result = listen(server_socket_descriptor, QUEUE_SIZE);
   if (listen_result < 0) {
       fprintf(stderr, "%s: Błąd przy próbie ustawienia wielkości kolejki.\n", argv[0]);
       exit(1);
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

   close(server_socket_descriptor);
   return(0);
}

