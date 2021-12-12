#!/bin/bash

set -e

printf "\n"
printf '==================================================================================\n'
printf '========== INICIADO SCRIPT DE RUN CONFIGURADO PARA HOT DEPLOY QUARKUS ============\n'
printf '==================================================================================\n'
printf "\n"

# funcao para escrever no arquivo .env
function writeFile {
  # grava as variaveis no arquivo .env
  {
   echo -n
   echo " "
   echo "ATF_USER=""$ATF_USER"
   echo "ATF_TOKEN=""$ATF_TOKEN"
   echo "ATF_COUNT=""$ATF_COUNT"
  } >> "$ENV_FILE"
  # remove linhas em branco no .env
  sed -i.bak -e '/^$/d' "$ENV_FILE"
  rm -rf "$ENV_FILE".bak
}

# funcao para obter o token do usuario no ATF
function getToken {
  echo "Informe as credenciais para acesso ao ATF"
  read -r -p "Chave do usuário no ATF: " ATF_USER
  # mudar para minusculo
  ATF_USER=$(echo "$ATF_USER" | tr '[:upper:]' '[:lower:]')
  if [[ -z "$ATF_USER" ]]; then
    echo "Por favor informe a chave do usuário em minúsculo (f/c/e/t/z) "
    exit 1
  fi
  if [[ "${#ATF_USER}" -gt 8 ]]; then
    echo "Por favor informe a chave do usuário com até 8 caracteres."
    exit 1
  fi
  read -r -sp "Senha do ATF/SisBB/LDAP: " ATF_PASSWORD
  if [[ -z "$ATF_PASSWORD" ]]; then
    echo "Por favor informe a senha."
    exit 1
  fi
  echo
  # transformando usuario:senha em base64
  ATF_REQ_TOKEN=$(echo "${ATF_USER}"":""${ATF_PASSWORD}" |base64 |awk '{print $1}'| sed 's/.$//')
  unset ATF_PASSWORD
  echo
  echo "Verificando conexão com o site ATF..."
  ATF_PING=$(ping -c 1 atf.intranet.bb.com.br 1>/dev/null; echo $?)
  if [[ $ATF_PING -eq 2 ]]; then
    echo
    echo "Erro: não foi possível conectar. Verifique a conexão com o site do ATF e/ou VPN e tente novamente."
    exit 1
  fi
  echo "Solicitando token de acesso no ATF. Aguarde.."
  # ATF_TOKEN=$(curl --noproxy "*.bb.com.br" -sb -X GET -k -H "Content-Type: application/json; charset=UTF-8" -H 'Authorization: Basic '${ATF_REQ_TOKEN}"="'' 'https://atf.intranet.bb.com.br/artifactory/api/security/encryptedPassword' | tail)
  ATF_TOKEN=$(curl -sb -X GET -k -H "Content-Type: application/json; charset=UTF-8" -H 'Authorization: Basic '${ATF_REQ_TOKEN}"="'' 'https://atf.intranet.bb.com.br/artifactory/api/security/encryptedPassword' | tail)
  echo
  if [[ ${#ATF_TOKEN} -ge 28 || -z "$ATF_TOKEN" ]] ; then
   echo "$ATF_TOKEN"
   echo "Erro ao baixar token. Verifique usuário e senha informados."
   exit 1
  else
   export ATF_USER=${ATF_USER}
   export ATF_TOKEN=${ATF_TOKEN}
  fi
}

# testa se o arquivo .env existe
ENV_FILE=.env
if [[ -f "$ENV_FILE" ]]; then
  # sim, o arquivo .env existe;
  # agora testa se o arquivo .env está vazio
  if [[ ! -s "$ENV_FILE" ]]; then
    echo "O arquivo $ENV_FILE estava vazio. Removendo..."
    rm -rf "$ENV_FILE"
    echo "Por favor rode novamente o script run/run.sh."
    exit 1
  else
    echo "O arquivo $ENV_FILE possui dados."
    sed -i.bak -e '/^$/d' "$ENV_FILE"
  fi

  # verifica se arquivo .env tem um contador, variavel ATF_COUNT
  ATF_COUNT_EXISTS=$(grep -c -s "^ATF_COUNT=.*" "$ENV_FILE" >/dev/null; echo $?)

  if [ "${ATF_COUNT_EXISTS}" -ne 0 ]; then
    # nao contem contador, pega o token e salva dados atuais, com o contador em 5
    getToken
    export ATF_COUNT=5
    writeFile
  else
    # contem o contador, entao seleciona o valor da expr ATF_COUNT=X para testar
    ATF_COUNT=$(grep -s "^ATF_COUNT=.*" "$ENV_FILE" |cut -c 11-)

    # agora testa se chegou no contador 0, em contagem regressiva para pedir token
    if [[ "$ATF_COUNT" -eq 1 ]]; then
      # contador=1, emtao salva dados atuais, limpa as variaveis do ATF e pega o token
      sed -i.bak -e 's/ATF_COUNT\=.*//g' "$ENV_FILE"
      sed -i.bak -e 's/ATF_USER\=.*//g' "$ENV_FILE"
      sed -i.bak -e 's/ATF_TOKEN\=.*//g' "$ENV_FILE"
      sed -i.bak -e '/^$/d' "$ENV_FILE"
      rm -rf "$ENV_FILE".bak
      getToken
      export ATF_COUNT=5
      writeFile
      unset ATF_COUNT
    fi
    if [[ "$ATF_COUNT" -gt 1 ]]; then
      # o contador pode ser 5,4,3,2.
      # ler variáveis do .env e decrementar o ATF_COUNT
      ATF_COUNT=$(grep "^ATF_COUNT" "$ENV_FILE" |cut -c 11-)
      ATF_USER=$(grep "^ATF_USER" "$ENV_FILE" |cut -c 10-)
      ATF_TOKEN=$(grep "^ATF_TOKEN" "$ENV_FILE" |cut -c 11-)
      # exportar variáveis que buscou no arquivo .env
      export ATF_USER
      export ATF_TOKEN
      # decrementar contador
      i=$((ATF_COUNT-1))
      sed -i.bak -e 's/ATF_COUNT\=.*/ATF_COUNT\='"$i"'/g' "$ENV_FILE"
      rm -rf "$ENV_FILE".bak
      unset ATF_COUNT
    fi
  fi
else
  # o arquivo .env NAO existe
  # pegando o token no ATF e criando o arquivo .env
  getToken
  export ATF_COUNT=5
  writeFile
fi

# corrigindo o M2_SETTINGS
M2_SETTINGS=~/.m2/settings.xml
if [[ -f "$M2_SETTINGS" ]]; then
 if [[ -n "$ATF_USER" && -n "$ATF_TOKEN" ]]; then
   # atualiza m2/settings.xml
   sed -i.bak -e "s/<username>.*/<username>""$ATF_USER""<\/username>/g" ~/.m2/settings.xml
   sed -i.bak -e "s/<password>.*/<password>""$ATF_TOKEN""<\/password>/g" ~/.m2/settings.xml
   # corrige porta 80 e http
   sed -i.bak -e 's|http://atf.intranet.bb.com.br:80/|https://atf.intranet.bb.com.br:443/|g' ~/.m2/settings.xml
   rm -rf ~/.m2/settings.xml.bak
 fi
fi

if [[ -n "$ATF_USER" && -n "$ATF_TOKEN" ]]; then
  echo
  echo "ATF Token armazenado/recuperado com sucesso."
fi
echo


printf "======= Path do Script de Carga do H2 no Contexto do Local =======\n"
export WORKSPACE="${PWD}/src/main/resources"
export myUID=${UID}
export myGroup=${GID}
printf ${WORKSPACE}
printf "\n"
printf "\n"

printf "============== Construido o Projeto com Maven Local ==============\n"
printf "\n"
mvn clean install
printf "\n"
printf "============ Projeto Construido com Sucesso pelo Maven ===========\n"
printf "\n"

printf "======= Path do Script de Carga do H2 no Contexto do Docker ======\n"
export WORKSPACE="/app/src/main/resources"
printf ${WORKSPACE}
printf "\n"
printf "\n"

printf "== Executando o Docker Compose Com Build para subir a aplicação ==\n"
printf "\n"

function ctrl_c() {
  printf "\n"
  printf '==========================================================================\n'
  printf '=========== EXECUTANDO O DOCKER COMPOSE DOWN APOS CTRL+C =================\n'
  printf '==========================================================================\n'
  printf "\n"
  docker-compose -f $PWD/run/docker-compose.yaml down
  exit
}
trap ctrl_c INT

docker-compose -f $PWD/run/docker-compose.yaml up --build

ctrl_c
