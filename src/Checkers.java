import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.net.*;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Checkers extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private static int width = 600, height = width;
	private static final int feltStorrelse = width / 8;
	private static final int antallFeltPerRad = width / feltStorrelse;
	private static int[][] baseSpillData = new int[antallFeltPerRad][antallFeltPerRad];
	private static int[][] spillData = new int[antallFeltPerRad][antallFeltPerRad];
	private static final int TOM = 0, ROD = 1, ROD_KONGE = 2, HVIT = 3, HVIT_KONGE = 4;
	public static JFrame frame;
	public boolean pagaendeSpill = true;
	private int navarendeSpiller = ROD;
	private boolean iSpill = false;
	private static int[][] tilgjengeligSpill = new int[antallFeltPerRad][antallFeltPerRad];
	private int lagretRad, lagretkolonne;
	private boolean hop = false;
	static BufferedImage crownImage = null;
	static String setning;
	static int PORT;
	static int PORT_C;
	static String IP;
	static Serializer Serial;
	static DatagramSocket klientSocket;
	static DatagramPacket sendPakke;
	static ByteBuffer buf;
	static String NAVN;
	static DatagramSocket serverSocket;
	static InetAddress adresse;
	static int KOL;
	static int RAD;
	static int RAD_S;
	static int KOL_S;

	static Checkers chk;

	public Checkers() {
		vindu(width, height, this);
		initialisererBrettet();
		repaint();
	}

	public boolean spillSlutt() {
		return spillSluttIntern(0, 0, 0, 0);
	}

	public boolean spillSluttIntern(int col, int row, int red, int white) {

		if (spillData[col][row] == ROD || spillData[col][row] == ROD_KONGE)
			red += 1;
		if (spillData[col][row] == HVIT || spillData[col][row] == HVIT_KONGE)
			white += 1;
		if (col == antallFeltPerRad - 1 && row == antallFeltPerRad - 1) {
			if (red == 0 || white == 0)
				return true;
			else
				return false;
		}
		if (col == antallFeltPerRad - 1) {
			row += 1;
			col = -1;
		}
		return spillSluttIntern(col + 1, row, red, white);
	}

	public void vindu(int width, int height, Checkers game) {
		JFrame ramme = new JFrame();
		ramme.setSize(width, height);
		ramme.setIconImage(crownImage);
		ramme.setBackground(Color.cyan);
		ramme.setLocationRelativeTo(null);
		ramme.pack();
		Insets insets = ramme.getInsets();
		int rammeVenstreGrense = insets.left;
		int rammeHoyreGrense = insets.right;
		int rammeTopGrense = insets.top;
		int rammeBunnGrense = insets.bottom;
		ramme.setPreferredSize(new Dimension(width + rammeVenstreGrense + rammeHoyreGrense,
				height + rammeBunnGrense + rammeTopGrense));
		ramme.setMaximumSize(new Dimension(width + rammeVenstreGrense + rammeHoyreGrense,
				height + rammeBunnGrense + rammeTopGrense));
		ramme.setMinimumSize(new Dimension(width + rammeVenstreGrense + rammeHoyreGrense,
				height + rammeBunnGrense + rammeTopGrense));
		ramme.setLocationRelativeTo(null);
		ramme.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ramme.addMouseListener(this);
		ramme.requestFocus();
		ramme.setVisible(true);
		ramme.add(game);
	}

	public void initialisererBrettet() {

		for (int col = 0; col < (antallFeltPerRad); col += 2) {
			spillData[col][5] = ROD;
			spillData[col][7] = ROD;
		}
		for (int col = 1; col < (antallFeltPerRad); col += 2)
			spillData[col][6] = ROD;
		for (int col = 1; col < (antallFeltPerRad); col += 2) {
			spillData[col][0] = HVIT;
			spillData[col][2] = HVIT;
		}
		for (int col = 0; col < (antallFeltPerRad); col += 2)
			spillData[col][1] = HVIT;
	}

	private static void tegnBrikke(int col, int row, Graphics g, Color color) {

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(color);

		g.fillOval((col * feltStorrelse) + 2, (row * feltStorrelse) + 2, feltStorrelse - 4, feltStorrelse - 4);
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		for (int row = 0; row < antallFeltPerRad; row++) {
			for (int col = 0; col < antallFeltPerRad; col++) {
				if ((row % 2 == 0 && col % 2 == 0) || (row % 2 != 0 && col % 2 != 0)) {
					baseSpillData[col][row] = 0;
					g.setColor(Color.gray);
					g.fillRect(col * feltStorrelse, row * feltStorrelse, feltStorrelse, feltStorrelse);
				} else {
					baseSpillData[col][row] = 1;
					g.setColor(Color.darkGray);
					g.fillRect(col * feltStorrelse, row * feltStorrelse, feltStorrelse, feltStorrelse);
				}
				if (sjekkLagBrikke(col, row) == true) {
					g.setColor(Color.darkGray.darker());
					g.fillRect(col * feltStorrelse, row * feltStorrelse, feltStorrelse, feltStorrelse);
				}
				if (tilgjengeligSpill[col][row] == 1) {
					g.setColor(Color.CYAN.darker());
					g.fillRect(col * feltStorrelse, row * feltStorrelse, feltStorrelse, feltStorrelse);
				}
				if (spillData[col][row] == HVIT)
					tegnBrikke(col, row, g, Color.white);
				else if (spillData[col][row] == HVIT_KONGE) {
					tegnBrikke(col, row, g, Color.white);
					g.drawImage(crownImage, (col * feltStorrelse) + 6, (row * feltStorrelse) + 6, feltStorrelse - 12,
							feltStorrelse - 12, null);
				} else if (spillData[col][row] == ROD)
					tegnBrikke(col, row, g, Color.red);
				else if (spillData[col][row] == ROD_KONGE) {
					tegnBrikke(col, row, g, Color.red);
					g.drawImage(crownImage, (col * feltStorrelse) + 6, (row * feltStorrelse) + 6, feltStorrelse - 12,
							feltStorrelse - 12, null);
				}
			}
		}
		if (spillSlutt() == true)
			spillSluttVisning(g);

	}

	private void spillSluttVisning(Graphics g) {
		String msg = "Game Over";
		Font small = new Font("Arial", Font.BOLD, 60);
		FontMetrics metr = getFontMetrics(small);
		g.setColor(Color.white);
		g.setFont(small);
		g.drawString(msg, (width - metr.stringWidth(msg)) / 2, width / 2);

	}

	private void nullstillSpill() {
		lagretkolonne = 0;
		lagretRad = 0;
		iSpill = false;
		hop = false;
		for (int row = 0; row < antallFeltPerRad; row++) {
			for (int col = 0; col < antallFeltPerRad; col++) {
				tilgjengeligSpill[col][row] = 0;
			}
		}
		repaint();
	}

	public void play(int col, int row) {

		if (iSpill == false && spillData[col][row] != 0 || iSpill == true && sjekkLagBrikke(col, row) == true) {
			nullstillSpill();
			lagretkolonne = col;
			lagretRad = row;
			hentTilgjengeligeSpill(col, row);

		} else if (iSpill == true && tilgjengeligSpill[col][row] == 1) {
			flytt(col, row, lagretkolonne, lagretRad);

		} else if (iSpill == true && tilgjengeligSpill[col][row] == 0) {
			nullstillSpill();

		}

	}

	public void mousePressed(java.awt.event.MouseEvent evt) {
		int col = (evt.getX() - 8) / feltStorrelse;
		int row = (evt.getY() - 30) / feltStorrelse;

		play(col, row);

		byte[] B = Serializer.SetData(col, row);

		sendPakke = new DatagramPacket(B, B.length, Checkers.adresse, Checkers.PORT_C);

		System.out.println(sendPakke.getAddress());

		try {
			Checkers.serverSocket.send(sendPakke);
		} catch (IOException e) {
			System.out.println("SENDING PRØVD OG FEILET");
			e.printStackTrace();
		}

	}

	private void byttSpiller() {
		if (navarendeSpiller == ROD)
			navarendeSpiller = HVIT;
		else
			navarendeSpiller = ROD;
	}

	private void flytt(int col, int row, int storedCol, int storedRow) {

		int x = spillData[storedCol][storedRow];
		spillData[col][row] = x;
		spillData[storedCol][storedRow] = TOM;
		sjekkKonge(col, row);
		if (hop == true)
			fjernBrikke(col, row, storedCol, storedRow);
		nullstillSpill();
		byttSpiller();
	}

	public boolean erKonge(int col, int row) {
		if (spillData[col][row] == ROD_KONGE || spillData[col][row] == HVIT_KONGE) {
			return true;
		} else
			return false;
	}

	private int sjekkMotspiller(int col, int row) {
		if (spillData[col][row] == ROD || spillData[col][row] == ROD_KONGE)
			return HVIT;
		else
			return ROD;
	}

	public void sjekkEkstraHop(int kol, int rad) {
		int motspiller = sjekkMotspiller(kol, rad);
		int motspillerKonge = sjekkMotspiller(kol, rad) + 1;
		if (spillData[kol - 1][rad - 1] == motspiller || spillData[kol - 1][rad - 1] == motspillerKonge) {
			tilgjengeligSpill[kol - 1][rad - 1] = 1;
		} else if (spillData[kol + 1][rad - 1] == motspiller || spillData[kol + 1][rad - 1] == motspillerKonge) {
			tilgjengeligSpill[kol + 1][rad - 1] = 1;
		} else if (spillData[kol - 1][rad + 1] == motspiller || spillData[kol - 1][rad + 1] == motspillerKonge) {
			tilgjengeligSpill[kol - 1][rad + 1] = 1;
		} else if (spillData[kol + 1][rad + 1] == motspiller || spillData[kol + 1][rad + 1] == motspillerKonge) {
			tilgjengeligSpill[kol + 1][rad + 1] = 1;
		}
		repaint();
	}

	private void sjekkKonge(int kol, int rad) {
		if (spillData[kol][rad] == ROD && rad == 0)
			spillData[kol][rad] = ROD_KONGE;
		else if (spillData[kol][rad] == HVIT && rad == antallFeltPerRad - 1)
			spillData[kol][rad] = HVIT_KONGE;
		else
			return;
	}

	private void fjernBrikke(int kol, int rad, int lagretKol, int lagretRad) {
		int brikkeRad = -1;
		int brikkeKol = -1;
		if (kol > lagretKol && rad > lagretRad) {
			brikkeRad = rad - 1;
			brikkeKol = kol - 1;
		}
		if (kol > lagretKol && rad < lagretRad) {
			brikkeRad = rad + 1;
			brikkeKol = kol - 1;
		}
		if (kol < lagretKol && rad > lagretRad) {
			brikkeRad = rad - 1;
			brikkeKol = kol + 1;
		}
		if (kol < lagretKol && rad < lagretRad) {
			brikkeRad = rad + 1;
			brikkeKol = kol + 1;
		}
		spillData[brikkeKol][brikkeRad] = TOM;
	}

	private void hentTilgjengeligeSpill(int kol, int rad) {

		iSpill = true;
		if ((sjekkLagBrikke(kol, rad) == true)) {
			if (spillData[kol][rad] == ROD) {
				gaOpp(kol, rad);
			}
			if (spillData[kol][rad] == HVIT) {
				gaNed(kol, rad);
			}
			if (spillData[kol][rad] == ROD_KONGE || spillData[kol][rad] == HVIT_KONGE) {
				gaOpp(kol, rad);

				gaNed(kol, rad);
			}
			repaint();
		}
	}

	private void gaOpp(int kol, int rad) {

		int radOpp = rad - 1;
		if (kol == 0 && rad != 0) {
			for (int i = kol; i < kol + 2; i++) {
				if (spillData[kol][rad] != 0 && spillData[i][radOpp] != 0) {
					if (kanHoppe(kol, rad, i, radOpp) == true) {
						int hopKol = kanHoppePos(kol, rad, i, radOpp)[0];
						int hopRad = kanHoppePos(kol, rad, i, radOpp)[1];
						tilgjengeligSpill[hopKol][hopRad] = 1;
					}
				} else if (baseSpillData[i][radOpp] == 1 && spillData[i][radOpp] == 0)
					tilgjengeligSpill[i][radOpp] = 1;
			}
		} else if (kol == (antallFeltPerRad - 1) && rad != 0) {
			if (spillData[kol][rad] != 0 && spillData[kol - 1][radOpp] != 0) {
				if (kanHoppe(kol, rad, kol - 1, radOpp) == true) {
					int hopKol = kanHoppePos(kol, rad, kol - 1, radOpp)[0];
					int hopRad = kanHoppePos(kol, rad, kol - 1, radOpp)[1];
					tilgjengeligSpill[hopKol][hopRad] = 1;
				}
			} else if (baseSpillData[kol - 1][radOpp] == 1 && spillData[kol - 1][radOpp] == 0)
				tilgjengeligSpill[kol - 1][radOpp] = 1;
		} else if (kol != antallFeltPerRad - 1 && kol != 0 && rad != 0) {
			for (int i = kol - 1; i <= kol + 1; i++) {
				if (spillData[kol][rad] != 0 && spillData[i][radOpp] != 0) {
					if (kanHoppe(kol, rad, i, radOpp) == true) {
						int jumpCol = kanHoppePos(kol, rad, i, radOpp)[0];
						int jumpRow = kanHoppePos(kol, rad, i, radOpp)[1];
						tilgjengeligSpill[jumpCol][jumpRow] = 1;
					}
				} else if (baseSpillData[i][radOpp] == 1 && spillData[i][radOpp] == 0)
					tilgjengeligSpill[i][radOpp] = 1;
			}
		}
	}

	private void gaNed(int kol, int rad) {

		int radNed = rad + 1;
		if (kol == 0 && rad != antallFeltPerRad - 1) {
			if (spillData[kol][rad] != 0 && spillData[kol + 1][radNed] != 0) {
				if (kanHoppe(kol, rad, kol + 1, radNed) == true) {
					int hoppKol = kanHoppePos(kol, rad, kol + 1, radNed)[0];
					int hoppRad = kanHoppePos(kol, rad, kol + 1, radNed)[1];
					tilgjengeligSpill[hoppKol][hoppRad] = 1;
				}
			} else if (baseSpillData[kol + 1][radNed] == 1 && spillData[kol + 1][radNed] == 0)
				tilgjengeligSpill[kol + 1][radNed] = 1;
		} else if (kol == antallFeltPerRad - 1 && rad != antallFeltPerRad - 1) {
			if (spillData[kol][rad] != 0 && spillData[kol - 1][radNed] != 0) {
				if (kanHoppe(kol, rad, kol - 1, radNed) == true) {
					int hoppKol = kanHoppePos(kol, rad, kol - 1, radNed)[0];
					int hopRad = kanHoppePos(kol, rad, kol - 1, radNed)[1];
					tilgjengeligSpill[hoppKol][hopRad] = 1;
				}
			} else if (baseSpillData[kol - 1][radNed] == 1 && spillData[kol - 1][radNed] == 0)
				tilgjengeligSpill[kol - 1][radNed] = 1;
		} else if (kol != antallFeltPerRad - 1 && kol != 0 && rad != antallFeltPerRad - 1) {
			for (int i = kol - 1; i <= kol + 1; i++) {
				if (spillData[kol][rad] != 0 && spillData[i][radNed] != 0) {
					if (kanHoppe(kol, rad, i, radNed) == true) {
						int hoppKol = kanHoppePos(kol, rad, i, radNed)[0];
						int hoppRad = kanHoppePos(kol, rad, i, radNed)[1];
						tilgjengeligSpill[hoppKol][hoppRad] = 1;
					}
				} else if (baseSpillData[i][radNed] == 1 && spillData[i][radNed] == 0)
					tilgjengeligSpill[i][radNed] = 1;
			}
		}
	}

	private boolean sjekkLagBrikke(int kol, int rad) {
		if (navarendeSpiller == ROD && (spillData[kol][rad] == ROD || spillData[kol][rad] == ROD_KONGE))
			return true;
		if (navarendeSpiller == HVIT && (spillData[kol][rad] == HVIT || spillData[kol][rad] == HVIT_KONGE))
			return true;
		else
			return false;
	}

	private boolean erTillatPos(int kol, int rad) {
		if (rad < 0 || rad >= antallFeltPerRad || kol < 0 || kol >= antallFeltPerRad)
			return false;
		else
			return true;
	}

	private boolean kanHoppe(int kol, int rad, int motspillerKol, int motspillerRad) {

		if (((spillData[kol][rad] == HVIT || spillData[kol][rad] == HVIT_KONGE)
				&& (spillData[motspillerKol][motspillerRad] == ROD
						|| spillData[motspillerKol][motspillerRad] == ROD_KONGE))
				|| (spillData[kol][rad] == ROD || spillData[kol][rad] == ROD_KONGE)
						&& (spillData[motspillerKol][motspillerRad] == HVIT
								|| spillData[motspillerKol][motspillerRad] == HVIT_KONGE)) {

			if (motspillerKol == 0 || motspillerKol == antallFeltPerRad - 1 || motspillerRad == 0
					|| motspillerRad == antallFeltPerRad - 1)
				return false;
			int[] tilData = kanHoppePos(kol, rad, motspillerKol, motspillerRad);
			if (erTillatPos(tilData[0], tilData[1]) == false)
				return false;
			if (spillData[tilData[0]][tilData[1]] == 0) {
				hop = true;
				return true;
			}
		}
		return false;
	}

	private int[] kanHoppePos(int kol, int rad, int motspillerKol, int motspillerRad) {
		if (kol > motspillerKol && rad > motspillerRad && spillData[kol - 2][rad - 2] == 0)
			return new int[] { kol - 2, rad - 2 };
		else if (kol > motspillerKol && rad < motspillerRad && spillData[kol - 2][rad + 2] == 0)
			return new int[] { kol - 2, rad + 2 };
		else if (kol < motspillerKol && rad > motspillerRad && spillData[kol + 2][rad - 2] == 0)
			return new int[] { kol + 2, rad - 2 };
		else
			return new int[] { kol + 2, rad + 2 };
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
	}
}
