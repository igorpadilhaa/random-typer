package com.github.igorpadilhaa.rndtyper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

public class Typer {

	private static final Font FONT = new Font("Courier", Font.BOLD, 64);

	public static void main(String[] args) {
		Frame window = new Frame();
		window.setIgnoreRepaint(true);
		window.setVisible(true);
		
		window.setMinimumSize(new Dimension(720, 480));
		window.setLocationRelativeTo(null);
		
		window.addWindowListener(new ShutdownListener());
		gameLoop(window);
	}

	public static void gameLoop(Frame window) {
		Game game = new Game();
		Keyboard keyboard = new Keyboard();

		window.createBufferStrategy(2);
		window.addKeyListener(keyboard);

		int frames = 0;
		int fps = 0;

		long time = 0;

		while (true) {
			try {
				Thread.sleep(1000 / 30);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			window.setTitle("Random Typer(fps: %d)".formatted(fps));
			game.render(window);
			game.update(keyboard);

			if (System.currentTimeMillis() - time >= 1000) {
				time = System.currentTimeMillis();
				fps = frames;
				frames = 0;
			}

			frames += 1;
		}
	}

	public static class Game {
		Track track = new Track(20);

		public void update(Keyboard board) {
			Character c = null;

			while ((c = board.next()) != null) {
				String t = track.ahead();

				if (t.charAt(0) != c)
					break;

				track.advance();
			}
		}

		public void render(Frame window) {
			BufferStrategy strat = window.getBufferStrategy();
			Graphics2D g = (Graphics2D) strat.getDrawGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, window.getWidth(), window.getHeight());

			g.setFont(FONT);

			Insets insets = window.getInsets();
			FontMetrics fm = g.getFontMetrics();

			String ahead = track.ahead();
			String before = track.before();

			int nw = fm.stringWidth(ahead.substring(0, 1));
			int x = window.getWidth() / 2 + insets.left;
			int y = window.getHeight() / 2 + insets.top;

			g.setColor(Color.LIGHT_GRAY);
			g.drawString(before, x - fm.stringWidth(before) - nw / 2, y - fm.getHeight() / 2);

			g.setColor(Color.RED);
			g.drawLine(x + nw / 2, y - (fm.getHeight() / 2) - fm.getLeading() - fm.getMaxAscent(), x + nw / 2,
					y - fm.getLeading());

			g.setColor(Color.BLACK);
			g.drawString(ahead, x - nw / 2, y - fm.getHeight() / 2);

			g.dispose();
			strat.show();
		}

	}

	public static List<Character> charList() {
		ArrayList<Character> list = new ArrayList<>();

		for (int i = 'a'; i <= 'z'; i++)
			list.add((char) i);

		for (int i = 'A'; i <= 'Z'; i++)
			list.add((char) i);

		list.trimToSize();
		Collections.shuffle(list);
		return Collections.unmodifiableList(list);
	}

	public static class Track {

		static List<Character> charset;
		static {
			charset = charList();
		}

		StringBuilder data;
		int length;

		RandomGenerator rnd;

		Track(int length) {
			rnd = random();

			this.length = length;
			this.data = new StringBuilder(length);

			if (this.length % 2 == 0)
				this.length += 1;

			initalize();
		}
		
		private static RandomGenerator random() {
			return new Random();
		}

		public void initalize() {
			while (data.length() < length)
				advance();
		}

		public void advance() {
			int index = rnd.nextInt(charset.size());

			Character c = charset.get(index);
			data.append(c);

			if (data.length() > length) {
				data.delete(0, data.length() - length);
			}
		}

		public String ahead() {
			return data.substring(length / 2, length);
		}

		public String before() {
			return data.substring(0, length / 2);
		}
	}

	public static class ShutdownListener extends WindowAdapter {

		@Override
		public void windowClosed(WindowEvent e) {
			windowClosing(e);
		}

		@Override
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}

	}

	public static class Keyboard extends KeyAdapter {

		Deque<Character> buffer = new ArrayDeque<>();

		@Override
		public void keyReleased(KeyEvent e) {
			char read = e.getKeyChar();
			buffer.push(read);
			e.consume();
		}

		public Character next() {
			if (buffer.isEmpty())
				return null;

			return buffer.pop();
		}

	}
}
