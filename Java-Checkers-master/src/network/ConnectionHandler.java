package network;

import java.awt.event.ActionEvent;
import java.net.Socket;

public class ConnectionHandler extends Thread {

	// Слушатель подключения
	private ConnectionListener listener;

	// Сокет, через который происходит соединение
	private Socket socket;

	// Конструктор класса
	public ConnectionHandler(ConnectionListener listener, Socket socket) {
		this.listener = listener;
		this.socket = socket;
	}

	// Метод для запуска потока
	@Override
	public void run() {

		// Если слушатель не задан, прерываем выполнение
		if (listener == null) {
			return;
		}

		// Отправляем событие обработчику
		ActionEvent e = new ActionEvent(this, 0, "CONNECTION ACCEPT");
		if (listener.getConnectionHandler() != null) {
			this.listener.getConnectionHandler().actionPerformed(e);
		}
	}

	// Геттер для слушателя подключения
	public ConnectionListener getListener() {
		return listener;
	}

	// Геттер для сокета
	public Socket getSocket() {
		return socket;
	}

}
