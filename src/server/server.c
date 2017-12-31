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


//struktura uzytkownikow
struct User
{
    int port;
    char name[20];
};


//struktura pokoju
struct Room
{
    int id;
    char name[20];
    char password[20];
    int port;
    int limit; //limit uzytkownikow
    int users;  //liczba obecnych w pokoju uzytkownikow
    struct User userList[MAX_USERS]; //zmiana w strukturze!!!!
};

//struktura wiadomosci
struct Message
{
    int  type;//dodane teraz bedzie uzywane przez server np. 1- wyslanie wiadomosci do innego klienta, 2. wyslij widomosc do pokoju 3. dolocz do pokoju 4. wyjdz z pokoju 5. stworz pokoj 6. wyloguj
    char text[240];
    char sender[20];
    char receiver[20];
    char date[40];
};


//struktura zawierajÄca dane, ktĂłre zostanÄ przekazane do wÄtku
struct thread_data_t
{
    int sfd;
    struct Room listaPokojow[10];
    struct User users[100];
};

struct thread_data_t *t_data_main; 




//TODO wysylanie danych do wszystkich klientow, cos jak to ponizej :
void sendDataToClients(struct thread_data_t th_data)
{
    int i;
    for(i=0;i<MAX_USERS;i++)
    {
        if(th_data.users[i].port != 0)
        {
            write(th_data.users[i].port, &th_data, sizeof(struct thread_data_t));
            printf("Wyslano główną strukturę do  : %d \n",th_data.users[i].port);
        }
    }
}

//funkcja opisujÄcÄ zachowanie wÄtku - musi przyjmowaÄ argument typu (void *) i zwracaÄ (void *)
void *ThreadBehavior(void *t_data)
{
    
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;
    //dostÄp do pĂłl struktury: (*th_data).pole
    char msg[240];
    while(1){
        /*struct Message m;
        strncpy(m.text, msg, sizeof(m.text));
        strncpy(m.sender, "server", sizeof(m.sender));
        strncpy(m.receiver, "client", sizeof(m.receiver));
        strncpy(m.date, "10-10-2010", sizeof(m.date));*/
        

    }
    pthread_exit(NULL);
}





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


void CreateNewClient(int connection_socket_descriptor) {
    //wynik funkcji tworzÄcej wÄtek
    int create_result = 0;

    //uchwyt na wÄtek
    pthread_t thread1;


    //dane, ktĂłre zostanÄ przekazane do wÄtku
    struct thread_data_t *th_data = (struct thread_data_t*)t_data_main;
    //struct thread_data_t *th_data = malloc(sizeof(struct thread_data_t));
    (*th_data).sfd = connection_socket_descriptor;

    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)th_data);
    if (create_result){
       printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
       exit(-1);
    }
    struct Message msg;      
	printf("Server oczekuje na funkcje jaka ma wykonac (zobacz zmieniana sktukture 'message') powinno dzialac tylko wysylanie wiadomosci do innego klienta po jego nazwie \n");
    if(read((*th_data).sfd,&msg,sizeof(msg))){
	int i = 0;	
	if(msg.type == 1){
	    while(i < MAX_USERS){
		if(strcmp((*th_data).users[i].name, msg.receiver) == 0){
		    write((*th_data).users[i].port, &msg, sizeof(msg));
			printf("Server powinien wyslac wiadomosc do odpowiedniego klienta\n")
		    break;		
		}
	    }
	}else if(msg.type == 2){  //nie dziala
	    while(i < 10){
		if(strcmp((*th_data).listaPokojow[i].name,msg.receiver) == 0){
			printf("Server nie robi nic ale poprawnie wywlales wiadomosc do wszystkich w pokoju \n");
		    //write((*th_data).users[i].port, &msg, sizeof(msg));
		    break;		
		}
 	    }
	}
    }
}


    

void CreateRoom(int connection_socket_descriptor) {
    //wynik funkcji tworzÄcej wÄtek
    int create_result = 0;

    //uchwyt na wÄtek
    pthread_t thread1;


    //dane, ktĂłre zostanÄ przekazane do wÄtku
    struct thread_data_t *th_data = (struct thread_data_t*)t_data_main;
    //struct thread_data_t *th_data = malloc(sizeof(struct thread_data_t));
    (*th_data).sfd = connection_socket_descriptor;

    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)th_data);
    if (create_result){
       printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
       exit(-1);
    }

    char msg[20];      
	printf("dd\n");
    if(read((*th_data).sfd,msg,sizeof(msg))){
	printf("ddddsdsdsdsd \n");
    }
}



//Funkcja ktora otwiera nowy port

int OpenNewSocket(int port){
   int server_socket_descriptor;
   int connection_socket_descriptor;
   int bind_result;
   int listen_result;
   char reuse_addr_val = 1;
   struct sockaddr_in server_address;
   
   t_data_main = malloc(sizeof(struct thread_data_t));

   //inicjalizacja gniazda serwera
   memset(&server_address, 0, sizeof(struct sockaddr));
   server_address.sin_family = AF_INET;
   server_address.sin_addr.s_addr = htonl(INADDR_ANY);
   server_address.sin_port = htons(port);

   server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);
   if (server_socket_descriptor < 0)
   {
       fprintf(stderr, "Błąd przy próbie utworzenia gniazda..\n");
       return 0;
   }
   setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&reuse_addr_val, sizeof(reuse_addr_val));

   bind_result = bind(server_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
   if (bind_result < 0)
   {
       fprintf(stderr, "Błąd przy próbie dowiązania adresu IP i numeru portu do gniazda.\n");
       return 0;
   }

   listen_result = listen(server_socket_descriptor, QUEUE_SIZE);
   if (listen_result < 0) {
       fprintf(stderr, "Błąd przy próbie ustawienia wielkości kolejki.\n");
       return 0;
   }
    connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);
    if (connection_socket_descriptor < 0)
    {
       fprintf(stderr, "Błąd przy próbie utworzenia gniazda dla połączenia.\n");
       return 0;
}

}







//funkcja obsĹugujÄca poĹÄczenie z nowym klientem
void handleConnection(int connection_socket_descriptor) {
    //wynik funkcji tworzÄcej wÄtek
    int create_result = 0;

    //uchwyt na wÄtek
    pthread_t thread1;


    //dane, ktĂłre zostanÄ przekazane do wÄtku
    struct thread_data_t *th_data = (struct thread_data_t*)t_data_main;
    //struct thread_data_t *th_data = malloc(sizeof(struct thread_data_t));
    (*th_data).sfd = connection_socket_descriptor;

    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)th_data);
    if (create_result){
       printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
       exit(-1);
    }

    char msg[128];      
    if(read((*th_data).sfd,msg,sizeof(msg))){
        int id = assignCPort((*th_data).users);
        int port = id + CLIENT_PORT;
        (*th_data).users[id].port = port;
        strncpy((*th_data).users[id].name, msg, sizeof(msg));
        
        int conv_port = htonl(port);
        write((*th_data).sfd, &conv_port, sizeof(conv_port));
        printf("client: %s chce uzyskać port \n",msg);
        sendDataToClients(*th_data);
	printf("Server otwiera nowy port na ktorym bedzie nasluchiwac\n");
	if(fork() == 0){
	}else{
	    OpenNewSocket(port);
	    CreateNewClient(port);
	    sendDataToClients(*th_data);
		printf("Udalo sie (chyba) stworzyc port dla nowego klienta prosze przelaczyc sie na nowo stworzony port\n");
	}    
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
   
   t_data_main = malloc(sizeof(struct thread_data_t));

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

