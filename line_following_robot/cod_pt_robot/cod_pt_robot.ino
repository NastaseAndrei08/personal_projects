#include <QTRSensors.h>
QTRSensors qtr;

const uint8_t SensorCount = 8;
uint16_t sensorValues[SensorCount];
int MOTOR2_PIN1 = 5;
int MOTOR2_PIN2 = 3;
int MOTOR1_PIN1 = 6;
int MOTOR1_PIN2 = 9;
int vitezaMS;
int vitezaMD;
int viteza_croaziera=90; /// se creste pana la maxim 255;
int poz_ref=35;
int Kp=15;   //se poate modifica  (acceleratie) valoare prea mare duce la instabilitate
int Kd=15;  //se poate modifica   (frana/redresare) valoare prea mare duce la instabilitate
int eroare;
int eroare_ant;
int derivativ;
int comanda;

void setup()
{

  pinMode(MOTOR1_PIN1, OUTPUT);
  pinMode(MOTOR1_PIN2, OUTPUT);
  pinMode(MOTOR2_PIN1, OUTPUT);
  pinMode(MOTOR2_PIN2, OUTPUT);
  // configure the sensors
  qtr.setTypeAnalog();
  qtr.setSensorPins((const uint8_t[]){A0, A1, A2, A3, A4, A5,A6,A7}, SensorCount);
  qtr.setEmitterPin(2);

  delay(500);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH); // turn on Arduino's LED to indicate we are in calibration mode

  for (uint16_t i = 0; i < 400; i++)
  {
    qtr.calibrate();
  }
  digitalWrite(LED_BUILTIN, LOW); // turn off Arduino's LED to indicate we are through with calibration

  // print the calibration minimum values measured when emitters were on
  Serial.begin(9600);
  for (uint8_t i = 0; i < SensorCount; i++)
  {
    Serial.print(qtr.calibrationOn.minimum[i]);
    Serial.print(' ');
  }
  Serial.println();

  // print the calibration maximum values measured when emitters were on
  for (uint8_t i = 0; i < SensorCount; i++)
  {
    Serial.print(qtr.calibrationOn.maximum[i]);
    Serial.print(' ');
  }
  Serial.println();
  Serial.println();
  delay(1000);
}

void loop()
{
  uint16_t position = qtr.readLineBlack(sensorValues)/100;
  Serial.println(position);

  eroare=poz_ref-position;
  derivativ=eroare-eroare_ant;
  comanda=Kp*eroare+Kd*derivativ;

  vitezaMS=viteza_croaziera+comanda;
  vitezaMD=viteza_croaziera-comanda;

  if(vitezaMS>=255)
  {vitezaMS=255;}

  if(vitezaMD>=255)
  {vitezaMD=255;}

  if(vitezaMS<=-255)
  {vitezaMS=-255;}

  if(vitezaMD<=-255)
  {vitezaMD=-255;}
 go(vitezaMS,vitezaMD);
   eroare_ant=eroare;
}



/*void go(int speedLeft, int speedRight) {
  if (speedLeft > 0) {
    analogWrite(MOTOR1_PIN1, speedLeft);
    analogWrite(MOTOR1_PIN2, 0);
  } 
  else {
    analogWrite(MOTOR1_PIN1, 0);
    analogWrite(MOTOR1_PIN2, -speedLeft);
  }
 
  if (speedRight > 0) {
    analogWrite(MOTOR2_PIN1, speedRight);
    analogWrite(MOTOR2_PIN2, 0);
  }else {
    analogWrite(MOTOR2_PIN1, 0);
    analogWrite(MOTOR2_PIN2, -speedRight);
  }
}*/

void go(int speedLeft, int speedRight) {
  // Reverse logic for one motor to correct direction
  if (speedLeft > 0) {
    analogWrite(MOTOR2_PIN1, speedLeft);  // Corrected: Left motor
    analogWrite(MOTOR2_PIN2, 0);
  } else {
    analogWrite(MOTOR2_PIN1, 0);          // Corrected: Left motor
    analogWrite(MOTOR2_PIN2, -speedLeft);
  }

  if (speedRight > 0) {
    analogWrite(MOTOR1_PIN1, speedRight); // Corrected: Right motor
    analogWrite(MOTOR1_PIN2, 0);
  } else {
    analogWrite(MOTOR1_PIN1, 0);          // Corrected: Right motor
    analogWrite(MOTOR1_PIN2, -speedRight);
  }
}

