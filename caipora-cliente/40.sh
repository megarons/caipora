

#!/bin/bash

counter=0

until [ $counter -gt 10 ]
do
    ttab -w mvnbb compile quarkus:dev -Ddebug=50$counter -Dquarkus.http.port=80$counter  
    sleep 15
    ((counter++))
done



