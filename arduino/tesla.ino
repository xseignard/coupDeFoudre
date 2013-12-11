#define THT 3
#define ECL 9

void setup() {
  Serial.begin(9600);
  pinMode(THT, OUTPUT);
  pinMode(ECL, OUTPUT);
  analogWrite(ECL, 80);
}

void loop() {
  // data received
  if (Serial.available() > 0) {
    char i = Serial.read();
    switch (i) {
      case '0':
        digitalWrite(THT, 0);
        break;
      case '1':
        digitalWrite(THT, 1);
        break;
      case '2':
        analogWrite(ECL, 80);
        break;
      case '3':
        analogWrite(ECL, 170);
        break;
      case '4':
        analogWrite(ECL, 255);
        break;
    }
  }
  Serial.println('0');
}
