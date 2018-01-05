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
#define MAX_ROOMS 10
#define CLIENT_PORT 2000
#define CLIENT_PORT_MSG 3000
#define CLIENT_PORT_WRITE 4000
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
    struct User users[10];
};

//struktura wiadomosci
struct Message
{
    //do konfiguracji/przelaczania
    int config;
    char text[240];
    char sender[20];
    char receiver[20];
    char date[40];
    char type[20];
};

struct Th_message
{
    int fd;
    int i;
    int id;
    struct Message msg;
};


struct data2send
{
    struct Room listaPokojow[MAX_ROOMS];
    struct User users[100];
};

//struktura zawierajÄ…ca dane, ktĂłre zostanÄ… przekazane do wÄ…tku
struct thread_data_t
{
    //socket prosby o przydzielenie portu
    int sfd;
    
    //users lobby sockets
    int uls[100];
    //users msg send sockets
    int ums[100];
    //users msg write sockets
    int umw[100];
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

void *SendMessageBehavior(void *t_message)
{
    //pthread_detach(pthread_self());
    struct Th_message *msg = (struct Th_message*)t_message;
    struct Message to_send = (*msg).msg;
    
     printf("Utworzono nowy wątek do wysłania wiadomości.\n",(*msg).i);
        
     int fd = accept((*t_data_main).umw[(*msg).i], NULL, NULL);
                        printf("accept\n");
                        
        printf("[server]: (%d, %s) - Wysyłanie wiadomości do (%d, %s)!\n", (*msg).id, (*t_data_main).data.users[(*msg).id].name, (*msg).i, (*t_data_main).data.users[(*msg).i].name);
        write(fd,&to_send, sizeof(to_send));
        close(fd);
        printf("[server]: (%d, %s) - (%d, %s) Odebrał wiadomość!\n", (*msg).id, (*t_data_main).data.users[(*msg).id].name, (*msg).i, (*t_data_main).data.users[(*msg).i].name);
        
        pthread_exit(NULL);
}

//funkcja opisujÄ…cÄ… zachowanie wÄ…tku - musi przyjmowaÄ‡ argument typu (void *) i zwracaÄ‡ (void *)
void *SendLobbyBehavior(void *arg)
{
    pthread_detach(pthread_self());
    int id = (int*) arg;
     //printf("%d\n", id);
    
    int connection_socket_descriptor = accept((*t_data_main).uls[id], NULL, NULL);
    if (connection_socket_descriptor < 0)
    {
        printf(": Błąd przy próbie utworzenia gniazda dla połączenia.\n");
        exit(1);
    }
    write(connection_socket_descriptor, &(*t_data_main).data, sizeof(struct data2send));
    printf("[server]: (%d, %s) - Wyslano lobby do klienta.\n", id, (*t_data_main).data.users[id].name);
    close(connection_socket_descriptor);
    pthread_exit(NULL);
}


void *ClientMsgBehavior(void *arg)
{
    pthread_detach(pthread_self());
    int id = (int*) arg;
    //printf("%d\n", id);
    
    printf("[server]: (%d, %s) - Stworzono nowy wątek do przetwarzania wiadomości. Czekanie na połączenie...\n", id, (*t_data_main).data.users[id].name);
    
	struct Message msg;
        char m[sizeof(struct Message)];
        /*struct Message m;
        strncpy(m.text, msg, sizeof(m.text));
        strncpy(m.sender, "server", sizeof(m.sender));
        strncpy(m.receiver, "client", sizeof(m.receiver));
        strncpy(m.date, "10-10-2010", sizeof(m.date));*/
        while(1)
        {
            int connection_socket_descriptor = accept((*t_data_main).ums[id], NULL, NULL);
            if (connection_socket_descriptor < 0)
            {
                printf(": Błąd przy próbie utworzenia gniazda dla połączenia.\n");
                exit(1);
            }
            printf("[server]: (%d, %s) - Nawiązano połączenie dla przetwarzania wiadomości!\n", id, (*t_data_main).data.users[id].name);
            printf("[server]: (%d, %s) - Oczekiwanie na wiadomość...\n", id, (*t_data_main).data.users[id].name);
            read(connection_socket_descriptor,&msg,sizeof(msg));
            printf("%d \n", sizeof(msg));
            close(connection_socket_descriptor);
            //TODO msg.config JEST ZLE CZYTANY (ZLA KONWERSJA Z JAVY?)
            
int i;
            printf("[server]: (%d, %s) - Odebrano wiadomość, przetwarzanie...\n", id, (*t_data_main).data.users[id].name);
            //printf("[server]: %d.\n%s.\n%s.\n%s.\n%s.\n",msg.config, msg.text, msg.sender, msg.receiver, msg.date);
            int ii = 0;
			printf("%s\n",msg.type);
			if(strncmp(msg.type,"11",2)==0){
				while(ii < MAX_USERS){
					//printf("(%d, %d, %s) - Odebrano wiadomość, przetwarzanie...\n", th_message.id, th_message.i, th_message.msg.text);
					printf("Wyslij widomosc\n");
					if(strncmp((*t_data_main).data.users[ii].name,msg.receiver,sizeof((*t_data_main).data.users[ii])) == 0){
							pthread_t thread;
							struct Th_message th_message;
							th_message.id = id;
							th_message.msg = msg;
							th_message.i = ii;
							int create_result = pthread_create(&thread, NULL, SendMessageBehavior, (void *)&th_message);
							if (create_result){
								printf("Błąd przy próbie utworzenia wątku ClientMsgBehavior, kod błędu: %d\n", create_result);
								exit(-1);
							}
							break;
						}
				   
					ii = ii + 1;
				}
			}else if(strncmp(msg.type,"12",2)==0){
				printf("dolacz do pokoju\n");
				while(ii < MAX_ROOMS){
					if(strncmp((*t_data_main).data.listaPokojow[ii].name,msg.receiver,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
						int iiw = 0;
						while(iiw < 10){
							if(strncmp((*t_data_main).data.listaPokojow[ii].users[iiw].name,"",20) == 0){
								printf("Przydzielono miejsce w Room\n");
								(*t_data_main).data.listaPokojow[ii].users[iiw] = (*t_data_main).data.users[id];
								pthread_t thread1[100];
								for (i=0; i<MAX_USERS; i++)
								{
									printf("Wysylam\n");
									//printf("%d ", (*th_data).data.users[i].port);
									if((*t_data_main).data.users[i].port != 0)
									{
										printf("cos sie dzieje\n");
										int create_result = pthread_create(&thread1[i], NULL, SendLobbyBehavior, (void*)i);
										 //printf("nowy watek\n");
										if (create_result){
										printf("Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
										exit(-1);
										}
									}
								}
								break;
							}
							iiw = iiw + 1;
						}
					}
					ii = ii + 1;
				}
			}else if(strncmp(msg.type,"13",2)==0){
				printf("Wyjdz z pokoju\n");
				while(ii < MAX_ROOMS){
					if(strncmp((*t_data_main).data.listaPokojow[ii].name,msg.receiver,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
						int iiw = 0;
						while(iiw < 10){
							if(strncmp((*t_data_main).data.listaPokojow[ii].users[iiw].name,msg.sender,sizeof((*t_data_main).data.listaPokojow[ii].users[iiw].name))==0){
								strcpy((*t_data_main).data.listaPokojow[ii].users[iiw].name,"");
								break;
							}
							iiw = iiw + 1;
						}
					}
					ii = ii + 1;
				}
			}else if(strncmp(msg.type,"14",2)==0){
				printf("Otrzymano prosbe o wylogowanie\n");
				printf("Nie testowano\n");
				while(ii < MAX_USERS){
					if(strncmp((*t_data_main).data.users[ii].name,msg.sender,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
						strncpy((*t_data_main).data.users[ii].name,"",20);
						break;
					}
				}
				
				int iiw = 0;
				while(iiw < MAX_ROOMS){
					while(ii < 10){
						if(strncmp((*t_data_main).data.listaPokojow[iiw].users[ii].name,msg.sender,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
							strncmp((*t_data_main).data.listaPokojow[iiw].users[ii].name,"",20);
						}
						ii = ii + 1;
					}
					iiw = iiw + 1;
				}
				printf("Nie znaleziono uz o podanym  niku");
			}else{
				printf("Nie poprawne gowno\n");
			}
        }
    
    pthread_exit(NULL);
}



//funkcja obsĹ‚ugujÄ…ca poĹ‚Ä…czenie z nowym klientem
void handleConnection(int connection_socket_descriptor) {
    //wynik funkcji tworzÄ…cej wÄ…tek
    int create_result = 0;

    //uchwyt na wÄ…tek
    pthread_t thread1[100];
    pthread_t thread2;


    //dane, ktĂłre zostanÄ… przekazane do wÄ…tku
    struct thread_data_t *th_data = (struct thread_data_t*)t_data_main;
    (*th_data).sfd = connection_socket_descriptor;
    struct data_lobby d_lobby;
    d_lobby.main_data = (*th_data);

    char msg[20];      
    if(read((*th_data).sfd,msg,sizeof(msg))){
        printf("[server]: (?, %s) - Prośba o przydzielenie portu...\n", msg);
        int id = assignCPort((*th_data).data.users);
        int port = id + CLIENT_PORT;
        (*th_data).data.users[id].port = port;
        strncpy((*th_data).data.users[id].name, msg, sizeof(msg));
        
        int conv_port = htonl(port);
        write((*th_data).sfd, &conv_port, sizeof(conv_port));
        printf("[server]: (?, %s) - Przydzielono port %d dla użytkwnika!\n", msg, port);
        
        create_result = pthread_create(&thread2, NULL, ClientMsgBehavior, (void*)id);
        if (create_result){
        printf("Błąd przy próbie utworzenia wątku ClientMsgBehavior, kod błędu: %d\n", create_result);
        exit(-1);
        }
        
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
    printf("\n[server]: Witaj w najlepszym IRCu na Twoim komputerze!\n\n");
    t_data_main = malloc(sizeof(struct thread_data_t));
    int connection_socket_descriptor;
    int server_socket_descriptor;
    server_socket_descriptor = createSocket(SERVER_PORT);
    
    //tworzenie socketow uls do lobby
    printf("[server]: (init) - Tworzenie deskryptorów serwera dla lobby...\n");
    int i;
    for(i=0; i<MAX_USERS; i++)
    {
        (*t_data_main).uls[i] = createSocket(CLIENT_PORT + i);
    }
    
    printf("[server]: (init) - Tworzenie pokojów...\n");
    for(i=0; i<MAX_ROOMS; i++)
    {
        strncpy((*t_data_main).data.listaPokojow[i].name, "Pokoj", 20);
		int ii = 0;
		while(ii < 10){
			strncpy((*t_data_main).data.listaPokojow[i].users[ii].name,"",20);
			ii = ii + 1;
		}
        printf("%s. \n", (*t_data_main).data.listaPokojow[i].name);
    }
    
        //tworzenie socketow uls do wysylania wiad
    printf("[server]: (init) - Tworzenie deskryptorów serwera dla wiadomości...\n");
    for(i=0; i<MAX_USERS; i++)
    {
        (*t_data_main).ums[i] = createSocket(CLIENT_PORT_MSG + i);
    }
    for(i=0; i<MAX_USERS; i++)
    {
        (*t_data_main).umw[i] = createSocket(CLIENT_PORT_WRITE + i);
    }
    printf("[server]: (OK) - Czekanie na nowego użytkownika...\n");
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
