import serial
import time

ser = serial.Serial('/dev/ttyUSB0')
ser.baudrate = 115200
i = 0
b = [0]
while True:
  b[0] = (i & 0xFF)
  ser.write(b);
  i += 1
  time.sleep(1)
ser.close

