package com.zholdak.lights.classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public final class Serial {

	private static final String SerialPortPrefix = "com"; 
	private static final int SerialBaudrate = SerialPort.BAUDRATE_115200; 

	public static final byte DATA_TERMINATOR = (byte)0xfe;
	public static final byte CONTROLLER_EXIT = (byte)0xfc;
	public static final byte RETURN_TO_PLAYER = (byte)0xfd;
	
	public static final int TIMEOUT = 3000; // millis
	public static final int MAX_BYTES = 1048576; // 1MB
	
	public static final byte SD_GET_FILE_LIST = (byte)0x10;
	public static final byte SD_DEL_FILE = (byte)0x11;
	public static final byte SD_GET_FILE = (byte)0x12;
	public static final byte SD_PUT_FILE = (byte)0x13;
	public static final byte SD_INFO = (byte)0x20;

	public static final byte SD_BYTE_OK = (byte)0xf0;
	public static final byte SD_BYTE_ERROR = (byte)0xf1;
	public static final byte SD_BYTE_TIMEOUT = (byte)0xf2;
	public static final byte SD_BYTE_EMPTY_FILENAME = (byte)0xf3;
	public static final byte SD_BYTE_CANT_DELETE = (byte)0xf4;
	public static final byte SD_BYTE_CANT_OPEN_FOR_WRITE = (byte)0xf5;
	public static final byte SD_BYTE_INCOMPLETE_DATA = (byte)0xf6;
	public static final byte SD_BYTE_CANT_WRITE = (byte)0xf7;
	public static final byte SD_BYTE_INIT_ERROR = (byte)0xf8;
	
	private SerialPort serialPort;
	
	public Serial(int num) throws SerialPortException {
		
		serialPort = new SerialPort(SerialPortPrefix+num);
		serialPort.openPort();
		serialPort.setParams(SerialBaudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
//		serialPort.addEventListener(new SerialPortEventListener() {
//			@Override
//			public void serialEvent(SerialPortEvent event) {
//				if( event.isRXCHAR() && event.getEventValue() > 0 ) { // && event.getEventValue() == 8 ) {
//					try {
//						byte buffer[] = serialPort.readBytes();
//						for( byte b: buffer ) {
//							System.out.println(String.format("%02X", b));
//						}
//					} catch (SerialPortException ex) {
//						System.out.println(ex);
//					}
//				}
//			}
//		}, SerialPort.MASK_RXCHAR);
	}
	
	public synchronized void writeBytes(byte[] bytes) throws SerialPortException, InterruptedException {
		serialPort.writeByte(DATA_TERMINATOR);
		writeBytesRaw(bytes);
		serialPort.writeByte(DATA_TERMINATOR);
	}
	public synchronized void writeBytesRaw(byte[] bytes) throws SerialPortException, InterruptedException {
		serialPort.writeBytes(bytes);
	}
	
	public boolean isOpened() {
		return serialPort.isOpened();
	}
	
	public void close() throws SerialPortException {
		serialPort.closePort();
	}
	
//	public void listen(final IOnIncomingBytes onIncomingBytes) throws SerialPortException {
//		serialPort.addEventListener(new SerialPortEventListener() {
//			public void serialEvent(SerialPortEvent event) {
//				if( event.isRXCHAR() && event.getEventValue() > 0 ) {
//					try {
//						if( !onIncomingBytes.bytesComing(serialPort.readBytes()) )
//							serialPort.removeEventListener();
//					} catch (SerialPortException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}, SerialPort.MASK_RXCHAR);
//	}
//	
//	public void cancelListen() {
//		try {
//			serialPort.removeEventListener();
//		} catch (SerialPortException e) {
//			e.printStackTrace();
//		}
//	}
	
	public byte[] readBytes(final byte terminator, final int timeout, final int maxBytesCount)
			throws RuntimeException, SerialPortException, InterruptedException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Timer timeoutTimer = new Timer(true);

		// Замок до окончания чтения 
		final CountDownLatch serialReadLatch = new CountDownLatch(1);

		serialPort.addEventListener(new SerialPortEventListener() {
			public void serialEvent(SerialPortEvent event) {
				if( event.isRXCHAR() && event.getEventValue() > 0 ) {
					try {
						for(byte b: serialPort.readBytes()) {
							if( b == terminator ) {
								serialPort.removeEventListener();
								serialReadLatch.countDown(); // снимаем замок
								break;
							}
							baos.write(new byte[]{b});
							if( baos.size() == maxBytesCount ) {
								serialPort.removeEventListener();
								serialReadLatch.countDown(); // снимаем замок
								break;
							}
						}
					} catch(IOException | SerialPortException e) {
						e.printStackTrace(); // this will never happen here but...
					}
				}
			}
		}, SerialPort.MASK_RXCHAR);
		
		// засечём тайм-аут ожидания замка чтения
		timeoutTimer.schedule(new TimerTask() {
			public void run() {
				try {
					serialPort.removeEventListener();
				} catch (SerialPortException e) {
					e.printStackTrace();
				} // тайм-аут, нуно прекратить слушать ответ на порту
				baos.reset(); // очистим буффер, если наступил тайм-аут
				while( serialReadLatch.getCount() > 0 ) // снимаем замок, если пришло время тайм-аута
					serialReadLatch.countDown();
			}
		}, timeout);
		// здесь останавливаемся и ждём пока не сняли замок
		serialReadLatch.await();
		timeoutTimer.cancel(); // отменим таймер
		try {
			serialPort.removeEventListener();
		} catch(Exception e) { }
		
		return baos.toByteArray();
	}
}
