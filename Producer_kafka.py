#Installation de kafka s'il n'est pas installé
!pip install kafka-python
from kafka import KafkaProducer
import json

# Configuration du producteur Kafka
producer = KafkaProducer(
    bootstrap_servers='kfakasda.eastus.cloudapp.azure.com:9092',
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Message JSON à envoyer
message ={
   "data":[
      {
         "confort":"Standart",
         "prix_base_per_km":2,
         "properties-client":{
            "logitude":3.3522,
            "latitude":36.8566,
            "nomclient":"Dia",
            "telephoneClient":"7774444"
         },
         "properties-driver":{
            "logitude":3.7038,
            "latitude":30.4168,
            "nomDriver":"Sow",
            "telephoneDriver":"0760786575"
         }
      }
   ]
}
# Envoi du message au topic 'test'
producer.send('kaza_1', value=message)

# Attente de la livraison de tous les messages
producer.flush()

print("Message envoyé avec succès.")