package ej2;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class clsVentana extends javax.swing.JFrame
{
  public static final String url = "https://www.taringa.net/";
  private JButton[] btnTops;
  private JPanel pan1;
  private JPanel pan2;
  private JPanel pan3;
  private JPanel pan4;
  private java.awt.Container contenedor;
  private JTextField[] titu;
  private JTextField[] autor;
  private JTextField[] puntos;
  
  public clsVentana()
  {
    setLayout(new java.awt.FlowLayout());
    setSize(1000, 700);
    setDefaultCloseOperation(3);
    contenedor = getContentPane();
    contenedor.setLayout(new java.awt.GridLayout(1, 3));
    
    pan1 = new JPanel();
    
    pan1.setLayout(new java.awt.GridLayout(5, 1));
    pan2 = new JPanel();
    pan2.setLayout(new javax.swing.BoxLayout(pan2, 1));
    pan3 = new JPanel();
    pan3.setLayout(new javax.swing.BoxLayout(pan3, 1));
    pan4 = new JPanel();
    pan4.setLayout(new javax.swing.BoxLayout(pan4, 1));
    


    titu = new JTextField[14];
    autor = new JTextField[14];
    puntos = new JTextField[14];
    armarPanelTexto(titu, pan2);
    armarPanelTexto(autor, pan3);
    armarPanelTexto(puntos, pan4);
    

    armarBotonera(btnTops);
    
    contenedor.add(pan1);
    contenedor.add(pan2);
    contenedor.add(pan4);
    contenedor.add(pan3);
  }
  
  private void armarPanelTexto(JTextField[] text1, JPanel gen) {
    for (int i = 0; i < 14; i++) {
      text1[i] = new JTextField();
      text1[i].setSize(250, 300);
      gen.add(text1[i]);
    }
  }
  
  private void armarBotonera(JButton[] botones) {
    String[] tops = { "HOY", "AYER", "SEMANA", "MES", "TODOS" };
    btnTops = new JButton[5];
    Oyente oyente = new Oyente();
    for (int i = 0; i < 5; i++) {
      btnTops[i] = new JButton(tops[i]);
      btnTops[i].addActionListener(oyente);
      pan1.add(btnTops[i]);
    }
  }
  
  public void llenar(clsScrap a1) {
    setearCampos(titu, a1.getTitulo());
    setearCampos(autor, a1.getPuntos());
    setearCampos(puntos, a1.getAutor());
  }
  
  private void setearCampos(JTextField[] text1, org.jsoup.select.Elements a1)
  {
    for (int i = 0; i < 14; i++)
      text1[i].setText(((org.jsoup.nodes.Element)a1.get(i)).text());
  }
  
  private void borrarCampos(JTextField[] text1) {
    for (int i = 0; i < 14; i++)
      text1[i].setText("");
  }
  
  private void borrar() {
    borrarCampos(titu);
    borrarCampos(autor);
    borrarCampos(puntos);
  }
  
  class Oyente implements java.awt.event.ActionListener {
    Oyente() {}
    
    public void actionPerformed(java.awt.event.ActionEvent evento) { clsScrap a1 = new clsScrap(null, null, null, null, "https://www.taringa.net/");
      String valor = evento.getActionCommand();
      if ("HOY".equals(valor)) {
        a1.buscaEntradas("ul.top-today");
        
        llenar(a1);
      }
      if ("AYER".equals(valor)) {
        a1.buscaEntradas("ul.top-yesterday");
        
        llenar(a1);
      }
      if ("SEMANA".equals(valor)) {
        a1.buscaEntradas("ul.top-week");
        
        llenar(a1);
      }
      if ("MES".equals(valor)) {
        a1.buscaEntradas("ul.top-month");
        
        llenar(a1);
      }
      if ("TODOS".equals(valor)) {
        a1.buscaEntradas("ul.top-all");
        
        llenar(a1);
      }
    }
  }
}
