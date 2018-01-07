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
    to_send.config = htonl(to_send.config);
    
     printf("[server]: Utworzono nowy wątek do wysłania wiadomości.\n");
     printf("test room id:%d i:%d",(*msg).id, (*msg).i);   
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
    int id = (int) arg;
     //printf("%d\n", id);
    
    int connection_socket_descriptor = accept((*t_data_main).uls[id], NULL, NULL);
    if (connection_socket_descriptor < 0)
    {
        printf("[server]: Błąd przy próbie utworzenia gniazda dla połączenia.\n");
        exit(1);
    }
    write(connection_socket_descriptor, &(*t_data_main).data, sizeof(struct data2send));
    printf("[server]: (%d, %s) - Wyslano lobby do klienta.\n", id, (*t_data_main).data.users[id].name);
    close(connection_socket_descriptor);
    pthread_exit(NULL);
}

void sendLobbyToAll()
{
    pthread_t threadLobby[100];
    int i;
    for (i=0; i<MAX_USERS; i++)
    {
        //printf("%d ", (*th_data).data.users[i].port);
        if((*t_data_main).data.users[i].port != 0)
        {

            pthread_create(&threadLobby[i], NULL, SendLobbyBehavior, (void*)i);
        }
    }
}

void *ClientMsgBehavior(void *arg)
{
    pthread_detach(pthread_self());
    int id = (int) arg;
    int active = 1;
    //printf("%d\n", id);
    
    printf("[server]: (%d, %s) - Stworzono nowy wątek do przetwarzania wiadomości. Czekanie na połączenie...\n", id, (*t_data_main).data.users[id].name);
    sendLobbyToAll();
	struct Message msg;
        /*struct Message m;
        strncpy(m.text, msg, sizeof(m.text));
        strncpy(m.sender, "server", sizeof(m.sender));
        strncpy(m.receiver, "client", sizeof(m.receiver));
        strncpy(m.date, "10-10-2010", sizeof(m.date));*/
        while(active)
        {
            int connection_socket_descriptor = accept((*t_data_main).ums[id], NULL, NULL);
            if (connection_socket_descriptor < 0)
            {
                printf("[server]: Błąd przy próbie utworzenia gniazda dla połączenia.\n");
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
			if(strncmp(msg.type,"11",2)==0){
				while(ii < MAX_USERS){
					//printf("(%d, %d, %s) - Odebrano wiadomość, przetwarzanie...\n", th_message.id, th_message.i, th_message.msg.text);
					printf("[server]: Wyslij widomosc\n");
					if(strncmp((*t_data_main).data.users[ii].name,msg.receiver,sizeof((*t_data_main).data.users[ii])) == 0){
							pthread_t thread;
                                                        pthread_t thread2;
                                                        
							struct Th_message th_message[2];
							th_message[0].id = id;
							th_message[0].msg = msg;
                                                        th_message[0].msg.config = 1;
							th_message[0].i = ii;
                                                        
                                                        th_message[1].id = ii;
							th_message[1].msg = msg;
                                                        strncpy(th_message[1].msg.receiver,msg.sender,sizeof(msg.sender));
                                                        strncpy(th_message[1].msg.sender,msg.receiver,sizeof(msg.sender));
                                                        th_message[1].msg.config = 1;
							th_message[1].i = id;
                                                        
                                                         pthread_create(&thread, NULL, SendMessageBehavior, (void *)&th_message[0]);//kontrola bledow #todo
                                                         pthread_create(&thread2, NULL, SendMessageBehavior, (void *)&th_message[1]);
							break;
						}
				   
					ii = ii + 1;
				}
			}else if(strncmp(msg.type,"12",2)==0){
				printf("[server]: dolacz do pokoju\n");
				while(ii < MAX_ROOMS){
					if(strncmp((*t_data_main).data.listaPokojow[ii].name,msg.receiver,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
						int iiw = 0;
						while(iiw < 10){
							if(strncmp((*t_data_main).data.listaPokojow[ii].users[iiw].name,"",20) == 0){
								printf("[server]: Przydzielono miejsce w Room\n");
								(*t_data_main).data.listaPokojow[ii].users[iiw] = (*t_data_main).data.users[id];
								pthread_t thread1[100];
								for (i=0; i<MAX_USERS; i++)
								{
									//printf("%d ", (*th_data).data.users[i].port);
									if((*t_data_main).data.users[i].port != 0)
									{
										//to chceck
										int create_result = pthread_create(&thread1[i], NULL, SendLobbyBehavior, (void*)i);
										 //printf("nowy watek\n");
										if (create_result){
										pthread_t threadl;
										printf("[server]: Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
										struct Th_message th_message[2];
										th_message[0].id = id;
										th_message[0].i = i;
										th_message[0].msg = msg;
										th_message[0].msg.config = 128; 
										strncpy(th_message[0].msg.text,"Nie udalo sie wyslac wiadomosci",sizeof("Nie udalo sie wyslac wiadomosci"));
										pthread_create(&threadl, NULL, SendMessageBehavior, (void *)&th_message[0]);//todo kontrola bledow
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
				printf("[server]: Klient poprosil o wyjscie z pokoju\n");
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
                                sendLobbyToAll();
			}else if(strncmp(msg.type,"14",2)==0){
				printf("[server]: Otrzymano prosbe o wylogowanie\n");
				while(ii < MAX_USERS){

					if(strncmp((*t_data_main).data.users[ii].name,msg.sender,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
						struct User newUser;
						(*t_data_main).data.users[ii] = newUser;
						pthread_t thread1[100];
						for (i=0; i<MAX_USERS; i++)
						{
							//printf("%d ", (*th_data).data.users[i].port);
							if((*t_data_main).data.users[i].port != 0)
							{
								int create_result = pthread_create(&thread1[i], NULL, SendLobbyBehavior, (void*)i);
								 //printf("nowy watek\n");
								if (create_result){
										//to check
										pthread_t threadl;
										printf("[server]: Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
										struct Th_message th_message[2];
										th_message[0].id = id;
										th_message[0].i = i;
										th_message[0].msg = msg;
										th_message[0].msg.config = 128; 
										strncpy(th_message[0].msg.text,"Nie udalo sie wyslac wiadomosci",sizeof("Nie udalo sie wyslac wiadomosci"));
										pthread_create(&threadl, NULL, SendMessageBehavior, (void *)&th_message[0]);//todo kontrola bledow
										
								}
							}
						}
				
						printf("[server]: Wylogowano !\n");
						break;
					}
					ii = ii + 1;
				}
				ii = 0;
				int iiw = 0;
				while(iiw < MAX_ROOMS){
					while(ii < 10){
						if(strncmp((*t_data_main).data.listaPokojow[iiw].users[ii].name,msg.sender,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
							strncmp((*t_data_main).data.listaPokojow[iiw].users[ii].name,"",20);
						}
						ii = ii + 1;
					}
					ii = 0;
					iiw = iiw + 1;
				}
                                active = 0;
                                sendLobbyToAll();
			}else if((strncmp(msg.type,"15",2)==0)){
				printf("[server]: Otrzymano prosbe o wyslanie wiadomosci do calego pokoju\n");
				printf("Jezeli dziala drogi if i poprawnie tworzy watki to powinno dzilac\n");
				while(ii < MAX_USERS){
					if(strncmp((*t_data_main).data.listaPokojow[ii].name,msg.receiver,sizeof((*t_data_main).data.listaPokojow[ii]).name) == 0){
						int iiw = 0;
						pthread_t thread1[10];
						while(iiw < 10){
							//printf("%d ", (*th_data).data.users[i].port);
							if((*t_data_main).data.listaPokojow[ii].users[iiw].port != 0)
							{
								printf("cos sie dzieje\n");
								int iiq = 0;
                                                                struct Th_message th_message[10];
								while(iiq < MAX_USERS){
									if(strncmp((*t_data_main).data.users[iiq].name,(*t_data_main).data.listaPokojow[ii].users[iiw].name,sizeof(*t_data_main).data.listaPokojow[ii].users[iiw].name) == 0){
										th_message[iiq].i = iiq;
										th_message[iiq].id = id;
										th_message[iiq].msg = msg;
										th_message[iiq].msg.config = 2;
										strncpy(th_message[iiq].msg.sender,(*t_data_main).data.listaPokojow[ii].name,sizeof((*t_data_main).data.listaPokojow[ii].name));
										strncpy(th_message[iiq].msg.type, msg.sender,sizeof(msg.sender));
										strncpy(th_message[iiq].msg.receiver, (*t_data_main).data.listaPokojow[ii].users[iiw].name,sizeof((*t_data_main).data.listaPokojow[ii].users[iiw].name));
																		//printf("przed wątkiem id:%d i:%d receiver:%s sender:%s config:%d",th_message.id, th_message.i, th_message.msg.receiver,th_message.msg.sender, th_message.msg.config );
										int create_result = pthread_create(&thread1[iiq], NULL, SendMessageBehavior, (void *)&th_message[iiq]);
										if (create_result){
											//to chceck
										pthread_t threadl;
										printf("[server]: Błąd przy próbie utworzenia wątku, kod błędu: %d\n", create_result);
										struct Th_message th_message[2];
										th_message[0].id = id;
										th_message[0].i = i;
										th_message[0].msg = msg;
										th_message[0].msg.config = 128; 
										strncpy(th_message[0].msg.text,"Nie udalo sie wyslac wiadomosci",sizeof("Nie udalo sie wyslac wiadomosci"));
										pthread_create(&threadl, NULL, SendMessageBehavior, (void *)&th_message[0]);//#todo kontrola bledow
										}
								
										
										break;
									}
									iiq = iiq + 1;
								}

							}
							iiw = iiw + 1;
						}
						//send msg to all
						break;
					}
				ii = ii + 1;
				}
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
    pthread_t thread2;

    //dane, ktĂłre zostanÄ… przekazane do wÄ…tku
    struct thread_data_t *th_data = (struct thread_data_t*)t_data_main;
    (*th_data).sfd = connection_socket_descriptor;

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
       sendLobbyToAll();
    }
}

int createSocket(int port)
{
   int server_socket_descriptor;
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
    
    strncpy((*t_data_main).data.listaPokojow[0].name, "Pierwszy!", 20);
    strncpy((*t_data_main).data.listaPokojow[1].name, "Nietykalni", 20);
    strncpy((*t_data_main).data.listaPokojow[2].name, "FFXIV", 20);
    strncpy((*t_data_main).data.listaPokojow[3].name, "Bazy Poprawka", 20);
    strncpy((*t_data_main).data.listaPokojow[4].name, "FSociety", 20);
    strncpy((*t_data_main).data.listaPokojow[5].name, "オタク", 20);
    strncpy((*t_data_main).data.listaPokojow[6].name, "BlaBlaBla", 20);
    strncpy((*t_data_main).data.listaPokojow[7].name, "for the horde", 20);
    strncpy((*t_data_main).data.listaPokojow[8].name, "Oblalem Egzamin Z Baz :(", 20);
    strncpy((*t_data_main).data.listaPokojow[8].name, "Bilderberg", 20);
    strncpy((*t_data_main).data.listaPokojow[9].name, "VIP Room", 20);
    strncpy((*t_data_main).data.listaPokojow[9].password, "1234", 20);
	
    for(i=0; i<MAX_ROOMS; i++)
    {
       
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
