# Routiner

Olá! Esté o Routiner, um serviço de gerenciamento de rotinas executáveis

## Como Rodar

Você pode executar o Routiner de 2 formas:

### Docker Container
 
Dê pull na imagem docker
```
sudo docker pull rodrigoom/routiner:0.1
```
Execute-a com o seguinte comando:
```
sudo docker run -p 7000:7000 -v /tmp/routiner-data:/tmp/routiner-data -e TZ='America/Sao_Paulo' rodrigoom/routiner:0.1
```

Os parâmetros adicionados ao run são para:
* **-p 7000:7000**: Expor a porta 7000 para o acesso ao serviço dentro do container
* **-v /tmp/routiner-data:/tmp/routiner-data**: As rotinas cadastradas no serviço 
escrevem mensagens em arquivos. Esse parâmetro monta um diretório local para dentro 
do container. Sem ele, os arquivos seriam escritos apenas dentro do container e não
seria possível acessá-los de fora, no seu gerenciador de arquivos por exemplo
* **-e TZ='America/Sao_Paulo'**: Configurar o timezone do container para um fuso horário
brasileiro, do contrário ele utiliza um fuso horário americano padrão da imagem de base 

### IDE
 
Para esse passo, você precisará ter o Maven instalado em sua máquina

Antes de executar, precisamos buildar o projeto e suas dependências
```
sudo mvn package
```

Dentro da sua IDE, execute a classe **App.class** para iniciar o serviço

## Como Utilizar

O Routiner funciona como um serviço tipicamente CRUD contendo um gerenciador de rotinas por trás.
As requisições possíveis são:

**GET** http://localhost:7000/routines - Lista todas as rotinas criadas 

**GET** http://localhost:7000/routines/:id - Detalha a rotina para o id correspondente

**POST** http://localhost:7000/routines - Cria uma nova rotina 

**DELETE** http://localhost:7000/routines/:id - Deleta a rotina para o id correspondente 

### Exemplos de requisições para testar

Criar rotinas
```
curl --request POST \
  --url http://localhost:7000/routines \
  --header 'content-type: application/json' \
  --data '{
    "interval_in_seconds": 2,
    "command": "write_to_file_one",
    "mensagem": "The Reason"
}'
```

```
curl --request POST \
  --url http://localhost:7000/routines \
  --header 'content-type: application/json' \
  --data '{
    "interval_in_seconds": 2,
    "command": "write_to_file_two",
    "mensagem": "No Bullshit"
}'
```

Listar rotinas existentes
```
curl --request GET \
  --url http://localhost:7000/routines
```

Buscar a rotina 1
```
curl --request GET \
  --url http://localhost:7000/routines/1
```

Deletar a rotina 1
```
curl --request DELETE \
  --url http://localhost:7000/routines/1
```

### E as rotinas? Fazem o que?

As rotinas podem receber dois comandos:

* *write_to_file_one*
* *write_to_file_two*

Esses comandos definem em qual arquivo a rotina 
escreverá a mensagem cadastrada nela

Por padrão, você vai encontrar os arquivos no 
seu diretório **/tmp/routiner-data**, com o nome
one.txt e two.txt

**Atenção**: Pode ser que você precise rodar o container docker como sudo.
Nesses casos, pode ser que seu arquivo fique travado para leitura/uso do usuário atual.
Pra resolver isso, só você rodar o comando:

```
sudo chmod a+rwx /tmp/routiner-data/
```

**Show, e o que vai ser escrito nos arquivos?**

A rotina se encarrega de escrever a **mensagem**, 
**qual rotina a escreveu** e **horário de escrita**.
Então você pode esperar um arquivo escrito da seguinte
maneira:

```
Own It - Executada pela rotina 1 em 2019-04-11 14:29:35
No Bullshit - Executada pela rotina 2 em 2019-04-11 14:29:40
Teamplay - Executada pela rotina 3 em 2019-04-11 14:29:45
Live The Ride - Executada pela rotina 4 em 2019-04-11 14:29:50
The Reason - Executada pela rotina 5 em 2019-04-11 14:29:55
Own It - Executada pela rotina 1 em 2019-04-11 14:30:00
The Reason - Executada pela rotina 2 em 2019-04-11 14:30:05
No Bullshit - Executada pela rotina 2 em 2019-04-11 14:30:10
The Reason - Executada pela rotina 2 em 2019-04-11 14:30:15
Live The Ride - Executada pela rotina 2 em 2019-04-11 14:30:20
The Reason - Executada pela rotina 2 em 2019-04-11 14:30:25
Own It - Executada pela rotina 2 em 2019-04-11 14:30:25
```

## Testes 

Existem testes cobrindo as principais funcionalidades do RoutineManager em como ele
gerencia as rotinas, da rotina em si na sua atualização e execução, e em endpoints do
service

Para rodar os testes, você pode rodar individualmente pela sua IDE, ou
com o comando

```
mvn test
```

## Como Funciona

O Routiner tem duas camadas, o serviço de CRUD de rotinas, e o gerenciador
de rotinas por trás. O grande desafio é não utilizar um sistema de Scheduler
já existente na maioria das linguagens, mas ser um por si só.

### Serviço

* Construído com o framework Javalin
* Contém 4 endpoints de rotina, 1 healthcheck, 
e tratamentos específicos para exceções.

**Considerações**
* Tornar o Controller das rotinas mais agnóstico para receber e devolver ***qualquer tipo
de comando*** dentro das rotinas seria uma evolução interessante

### RoutineManager

* Cada rotina criada é inserida em um ***mapa de Id->Rotina***, e enviada como parâmetro
para o método que calcula a próxima execução a ser feita
* Este método calcula em qual momento a próxima execução será feita, insere
a rotina em uma ***fila*** de execuções, e **reordena** a fila por ordem crescente
de momento de execução
* Ao final, notifica uma variável ***semaphore***
* Em uma thread separada, um método roda com um ***while true*** checando a fila
de execuções. Caso haja algo na fila, a thread usa a variável ***semaphore***
para ficar em await durante o tempo necessário para a próxima execução
* Caso haja alguma notificação na variável ***semaphore***, o método faz uma nova checagem
da fila para saber qual a próxima execução e quanto tempo até ela
* Dado este tempo, o método utiliza uma ***threadpool*** para executar a rotina que está na fila
* A rotina executada se encarrega de rodar o seu comando ***runnable***
e atualizar suas propriedades de histórico de execução, contagem de execuções,
etc.

**Considerações**

* O conceito de Routine e de Command ficaram separados pra que fosse fácil
criar rotinas capazes de executar **qualquer** tipo de comando com facilidade.
Isso se mostra útil tanto para os testes, quanto para facilitar a implementação
de outros tipos de comandos caso necessário 

