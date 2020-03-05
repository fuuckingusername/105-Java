package BouncingBallSimple;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The control logic and main display panel for game.
 */
public class BallWorld extends JPanel {
	private static final int UPDATE_RATE = 30; // Frames per second (fps)

	private Ball ball; // A single bouncing Ball's instance
	private ContainerBox box; // The container rectangular box

	private DrawCanvas canvas; // Custom canvas for drawing the box/ball
	private int canvasWidth;
	private int canvasHeight;

	private ControlPanel control;
	private boolean paused;

	/**
	 * Constructor to create the UI components and init the game objects. Set
	 * the drawing canvas to fill the screen (given its width and height).
	 * 
	 * @param width
	 *            : screen width
	 * @param height
	 *            : screen height
	 */
	public BallWorld(int width, int height) {

		canvasWidth = width;
		canvasHeight = height;

		// Init the ball at a random location (inside the box) and moveAngle
		Random rand = new Random();
		int radius = 100;
		int x = rand.nextInt(canvasWidth - radius * 2 - 20) + radius + 10;
		int y = rand.nextInt(canvasHeight - radius * 2 - 20) + radius + 10;
		int speed = 5;
		int angleInDegree = rand.nextInt(360);
		ball = new Ball(x, y, radius, speed, angleInDegree, Color.BLUE);

		// Init the Container Box to fill the screen
		box = new ContainerBox(0, 0, canvasWidth, canvasHeight, Color.BLACK, Color.WHITE);

		// Init the custom drawing panel for drawing the game
		canvas = new DrawCanvas();
		this.setLayout(new BorderLayout());
		this.add(canvas, BorderLayout.CENTER);

		// Handling window resize.
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Component c = (Component) e.getSource();
				Dimension dim = c.getSize();
				canvasWidth = dim.width;
				canvasHeight = dim.height;
				// Adjust the bounds of the container to fill the window
				box.set(0, 0, canvasWidth, canvasHeight);
			}
		});

		control = new ControlPanel();

		// Layout the drawing panel and control panel
		this.setLayout(new BorderLayout());
		this.add(canvas, BorderLayout.CENTER);
		this.add(control, BorderLayout.SOUTH);

		// Start the ball bouncing
		gameStart();
	}

	/** Start the ball bouncing. */
	public void gameStart() {
		// Run the game logic in its own thread.
		Thread gameThread = new Thread() {
			public void run() {
				while (true) {
					// Execute one time-step for the game
					long beginTimeMillis, timeTakenMillis, timeLeftMillis;
					beginTimeMillis = System.currentTimeMillis();

					if (!paused) {
						// Execute one game step
						gameUpdate();
						// Refresh the display
						repaint();
					}
//					gameUpdate();
//					// Refresh the display
//					repaint();
//					// Delay and give other thread a chance
					try {
						Thread.sleep(1000 / UPDATE_RATE);
					} catch (InterruptedException ex) {
					}
				}
			}
		};
		gameThread.start(); // Invoke GaemThread.run()
	}

	/**
	 * One game time-step. Update the game objects, with proper collision
	 * detection and response.
	 */
	public void gameUpdate() {
		ball.moveOneStepWithCollisionDetection(box);
	}

	/** The custom drawing panel for the bouncing ball (inner class). */
	class DrawCanvas extends JPanel {
		/** Custom drawing codes */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g); // Paint background
			// Draw the box and the ball
			box.draw(g);
			ball.draw(g);
			// Display ball's information
			g.setColor(Color.WHITE);
			g.setFont(new Font("Courier New", Font.PLAIN, 12));
			g.drawString("Ball " + ball.toString(), 20, 30);
		}

		/** Called back to get the preferred size of the component. */
		@Override
		public Dimension getPreferredSize() {
			return (new Dimension(canvasWidth, canvasHeight));
		}
	}

	/** The control panel (inner class). */
	class ControlPanel extends JPanel {

		private JSlider radiusControl;

		/** Constructor to initialize UI components of the controls */
		public ControlPanel() {
			// A checkbox to toggle pause/resume movement
			JCheckBox pauseControl = new JCheckBox();
			this.add(new JLabel("Pause"));
			this.add(pauseControl);
			pauseControl.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					paused = !paused; // Toggle pause/resume flag
				}
			});

			// A slider for adjusting the speed of the ball
			int minSpeed = 2;
			int maxSpeed = 20;
			JSlider speedControl = new JSlider(JSlider.HORIZONTAL, minSpeed, maxSpeed, (int) ball.getSpeed());
			this.add(new JLabel("Speed"));
			this.add(speedControl);
			speedControl.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						int newSpeed = (int) source.getValue();
						int currentSpeed = (int) ball.getSpeed();
						ball.speedX *= (float) newSpeed / currentSpeed;
						ball.speedY *= (float) newSpeed / currentSpeed;
					}
				}
			});

			// A slider for adjusting the radius of the ball
			int minRadius = 10;
			int maxRadius = ((canvasHeight > canvasWidth) ? canvasWidth : canvasHeight) / 2 - 8;
			radiusControl = new JSlider(JSlider.HORIZONTAL, minRadius, maxRadius, (int) ball.radius);
			this.add(new JLabel("Ball Radius"));
			this.add(radiusControl);
			radiusControl.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider) e.getSource();
					if (!source.getValueIsAdjusting()) {
						float newRadius = source.getValue();
						ball.radius = newRadius;
						// Reposition the ball such as it is inside the box
						if (ball.x - ball.radius < box.minX) {
							ball.x = ball.radius + 1;
						} else if (ball.x + ball.radius > box.maxX) {
							ball.x = box.maxX - ball.radius - 1;
						}
						if (ball.y - ball.radius < box.minY) {
							ball.y = ball.radius + 1;
						} else if (ball.y + ball.radius > box.maxY) {
							ball.y = box.maxY - ball.radius - 1;
						}
					}
				}
			});
		}
	}
}