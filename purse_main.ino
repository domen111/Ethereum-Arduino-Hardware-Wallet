#define D2 2
#define D3 3
#define D4 4
#define D5 5
#define D6 6
#define D7 7
#define D8 8
#define D9 9
#define D10 10
#define D11 11
#define D12 12

#define NUM_KEY_MIN 1
#define NUM_KEY_MAX 7

#define KEY_STAT_FREE  0x0000
#define KEY_STAT_HOLD  0x0001
#define KEY_STAT_DOWN  0x1000
#define KEY_STAT_UP    0x2000

#define KEY_TRANS_TIME 50

#define KEY_ENTER 0
#define KEY_


int key_to_pin [] = { 4,3,2,5, 9,11,10,8 };
char pin_to_key[] = { 0,0,
                      2,1,0,3,   // D2 ~ D5
                      0,0,
                      7,4,6,5 }; // D8 ~ D11

void ReadStartSignal();
void InputPasswd();

void setup()
{
  Serial.begin(9600);
  pinMode( LED_BUILTIN, OUTPUT );
  for(int i=NUM_KEY_MIN; i<=NUM_KEY_MAX; i++)
    pinMode( key_to_pin[i], INPUT );
}

void loop()
{
  ReadStartSignal();
  for(int i=0; i<2; i++)
  {
    digitalWrite( LED_BUILTIN, HIGH );
    delay(100);
    digitalWrite( LED_BUILTIN, LOW );
    delay(100);
  }
  InputPasswd();
}


void ReadStartSignal()
{
  int i=0;
  char s[] = "ooo";
  while(i<3)
  {
    while(!Serial.available()<0)
      ;
    int c = Serial.read();
    if(c == s[i])
      i++;
  }
  Serial.write("ok");
  return;
}


void InputPasswd()
{
  byte pwd[16] = { 1,2,3 };
  byte pwdlen = 3;
  byte s[16];
  byte s_len = 0;
  int keyStat[16];
  
  keyStat[0] = 0;
  for(int i=NUM_KEY_MIN; i<=NUM_KEY_MAX; i++)
    keyStat[i] = 0;
  
  while(true)
  {
    for(int i=0; i<=NUM_KEY_MAX; i++)
    {
      int x = digitalRead(key_to_pin[i]);
      if( keyStat[i] != x )
      {
        keyStat[i] = x;
        if(x==1)
        {
          char c = '0'+i;
          Serial.write( &c, 1 );
          analogWrite(13,250);
          QuickFlickFor(100);
        }
        else
          delay(100);
      }
    }
    digitalWrite(13,LOW);
  }
}


void QuickFlickFor(int t)
{
  for(int i=1; i<t; i++)
  {
    digitalWrite(13,HIGH);
    digitalWrite(13,LOW);
    delay(1);
  }
}

