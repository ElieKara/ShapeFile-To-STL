package ummisco.map.shpToStl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Controleur implements ActionListener{

	private ArrayList<String> liste_shapefile = new ArrayList<String>();
	private ArrayList<JButton> liste_bouton = new ArrayList<JButton>();
	private ArrayList<JLabel> liste_nomfichier = new ArrayList<JLabel>();
	private JFrame fenetre;
	private FileFilter shp;
	private JPanel panel;

	public Controleur(JFrame fenetre, JPanel panel){
		this.fenetre=fenetre;
		this.panel=panel;
		this.shp = new FileNameExtensionFilter("ShapeFile","shp");
	}


	//Verifit le bouton clique
	@Override
	public void actionPerformed(ActionEvent e) {
		String text = e.getActionCommand();
		if(text.equals("Nouveau ShapeFile")){
			choixFichier();
		}
		else if(text.equals("Convertir en STL")){

		}
		else{
			supprimeShapeFile(e);
		}	
		fenetre.revalidate();
		fenetre.repaint();
	}


	//Ajoute un ShapeFile
	public void choixFichier(){
		JFileChooser exploreur = new JFileChooser(".");
		exploreur.setFileFilter(shp);
		int res = exploreur.showOpenDialog(fenetre);
		if(res==JFileChooser.APPROVE_OPTION){
			String nomfichier = exploreur.getSelectedFile().getName();
			JLabel fichier = new JLabel(nomfichier);
			fichier.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(fichier);
			JButton bouton = new JButton("Supprimer");
			bouton.setActionCommand(nomfichier);
			bouton.addActionListener(this);
			panel.add(bouton);
			liste_shapefile.add(nomfichier);
			liste_nomfichier.add(fichier);
			liste_bouton.add(bouton);
		}
	}


	//Renvoit la liste des noms des fichier shapefile
	public ArrayList<String> getListeShapeFile(){
		return liste_shapefile;
	}


	//Supprime ShapeFile
	public void supprimeShapeFile(ActionEvent e){
		if(liste_shapefile.size()!=0){
			int index = liste_shapefile.indexOf(e.getActionCommand());
			liste_shapefile.remove(index);
			panel.remove(liste_bouton.get(index));
			panel.remove(liste_nomfichier.get(index));
			liste_nomfichier.remove(index);
			liste_bouton.remove(index);
		}
	}
}
