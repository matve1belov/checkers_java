package network;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener extends Thread {

	/** Сокет, который будет слушать соединения. */
	private ServerSocket serverSocket;

	/** Обработчик действия, который будет вызываться при подключении. */
	private ActionListener connectionHandler;

	public ConnectionListener() {
		this(0);
	}

	public ConnectionListener(int port) {
		setPort(port);
	}

	public ConnectionListener(int port, ActionListener connectionHandler) {
		setPort(port);
		this.connectionHandler = connectionHandler;
	}

	public void listen() {
		start();
	}

	@Override
	public void run() {

		// Особые случаи
		if (serverSocket == null) {
			return;
		}
		if (serverSocket.isClosed()) {
			try {
				this.serverSocket = new ServerSocket(
						serverSocket.getLocalPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Слушаем входящие попытки подключения к серверу
		while (!serverSocket.isClosed()) {
			try {

				// Получаем соединение и обрабатываем его
				ConnectionHandler conn = new ConnectionHandler(
						this, serverSocket.accept());
				conn.start();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean stopListening() {

		// Особые случаи
		if (serverSocket == null || serverSocket.isClosed()) {
			return true;
		}

		// Пытаемся закрыть соединение
		boolean err = false;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			err = true;
		}

		return !err;
	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	public void setPort(int port) {

		// Остановить сервер, если он работает
		stopListening();

		// Создаем новый сокет сервера (сервер нужно перезапустить)
		try {
			if (port < 0) {
				this.serverSocket = new ServerSocket(0);
			} else {
				this.serverSocket = new ServerSocket(port);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public ActionListener getConnectionHandler() {
		return connectionHandler;
	}

	public void setConnectionHandler(ActionListener connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	public static String read(Socket socket) {

		if (socket == null) {
			return "";
		}

		// Читаем все данные из потока
		String data = "";
		try {
			InputStream in = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = br.readLine()) != null) {
				data += line + "\n";
				if (!br.ready()) {break;}
			}
			// Оставляем только непустые данные
			if (!data.isEmpty()) {
				data = data.substring(0, data.length()-1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	// Проверяем, доступен ли порт
	public static boolean available(int port) {
		
		if (port < 0 || port > 65535) {
			return false;
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {}
			}
		}

		return false;
	}
}
