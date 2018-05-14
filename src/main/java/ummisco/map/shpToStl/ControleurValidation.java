package ummisco.map.shpToStl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class ControleurValidation implements ActionListener{
	
	private ArrayList<File> liste_shapefile = new ArrayList<File>();
	private JFrame fenetre;
	private JFrame fenetredebut;
	private JFormattedTextField decoupex;
	private JFormattedTextField decoupey;
	private JTextField hauteur;

	public ControleurValidation(JFrame fenetredebut,JFrame fenetre,JFormattedTextField decoupex,JFormattedTextField decoupey,JTextField hauteur,ArrayList<File> liste_shapefile ){
		this.fenetre=fenetre;
		this.fenetredebut=fenetredebut;
		this.liste_shapefile=liste_shapefile;
		this.decoupex=decoupex;
		this.decoupey=decoupey;
		this.hauteur=hauteur;
		
	}

	
	//Verifit le bouton clique
	@Override
	public void actionPerformed(ActionEvent e) {
		String text = e.getActionCommand();
		if(text.equals("Retour")){
			fenetredebut.setVisible(true);
			fenetre.setVisible(false);
		}
		if(text.equals("OK")){
			int coupex,coupey;
			String haut;
			if(decoupex.getText().equals(""))
				coupex=0;
			else
				coupex = Integer.parseInt(decoupex.getText());
			if(decoupey.getText().equals(""))
				coupey=0;
			else
				coupey = Integer.parseInt(decoupey.getText());
			if(hauteur.getText().equals(""))
				haut="Valeur par defout ?";
			else
				haut = hauteur.getText();
			Conversion conv = new Conversion(liste_shapefile,coupex,coupey,haut);	
			try {
				conv.parcoursFichier();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JOptionPane.showMessageDialog(fenetre,"Termine !","", JOptionPane.INFORMATION_MESSAGE);
			fenetre.dispose();
			fenetredebut.dispose();
			Interface fenetre = new Interface();
		}
	}
}
