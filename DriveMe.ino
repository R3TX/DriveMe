
#include <Shieldbot.h>

Shieldbot shieldbot = Shieldbot();

int accion = ' ';

void setup() {
  Serial.begin(9600);//Se inicia el puerto serial 
}

void loop()
{
    if(Serial.available()>0){        
        accion = Serial.read();     // Lee puerto serial
        shieldbot.setMaxSpeed(255); //Se setea la velocidad máxima que podra tomar el shieldbot
    } 
    if(accion=='f'){           // Frente
      shieldbot.drive(127,127);
    }
    if(accion=='r'){          // Reversar
      shieldbot.drive(-128,-128);      
    }
    if(accion=='i'){          // Mover a la izquierda
      shieldbot.drive(0,127);  
    }
    if(accion=='d'){          // Mover a la derecha
      shieldbot.drive(127,0);  
    } 
    if(accion=='s'){         //Parar
      shieldbot.drive(0,0);  
    }
}
