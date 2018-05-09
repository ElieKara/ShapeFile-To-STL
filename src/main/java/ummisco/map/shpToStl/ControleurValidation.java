package ummisco.map.shpToStl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ControleurValidation implements ActionListener{
	private ArrayList<File> liste_shapefile = new ArrayList<File>();
	private ArrayList<JButton> liste_bouton = new ArrayList<JButton>();
	private ArrayList<JLabel> liste_nomfichier = new ArrayList<JLabel>();
	private ArrayList<String> liste_cpt = new ArrayList<String>();
	private JFrame fenetre;
	private JPanel panel;

	public ControleurValidation(JFrame fenetre, JPanel panel){
		this.fenetre=fenetre;
		this.panel=panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		
	}
}
