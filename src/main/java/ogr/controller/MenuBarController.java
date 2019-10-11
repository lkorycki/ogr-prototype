package ogr.controller;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import ogr.MApplication;
import ogr.OCR;
import ogr.ServerClient;
import ogr.engine.ImageProcessor.FilterColor;
import ogr.model.Item;
import ogr.util.FileUtils;
import ogr.util.GraphFileConverter;
import ogr.view.MainWindowView;
import ogr.view.ServerWindowView;

public class MenuBarController extends Controller {
	
	private MainWindowView mainWindow;
	
	public MenuBarController(MApplication mapp) {
		super(mapp);
		mainWindow = getMApp().getMainWindow();
	}
	
	public ActionListener getLoadFileListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File f = showLoadFileDialog();
				if (f != null) {
					System.out.println(f);
					FileUtils fu = new FileUtils(getMApp());
					fu.addFile(f.getParent(), f.getName());
				}
			}
		};
	}
	
	public ActionListener getLoadFileFromServerListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ServerWindowController swc = new ServerWindowController(getMApp());
				new ServerWindowView(swc);
			}
		};
	}
	
	public ActionListener getExitListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
	}
	
	public ActionListener getShowAboutListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutText = "Project \"OCR for Graphs\" was realized in cooperation\n"
						+ "with Nokia Wroc³aw and Wroc³aw University of Technology\n"
						+ "as a part of Konferencja Projektów Zespo³owych\nand Innovative Projects programs.\n\n"
		    			+ "Authors:\n£ukasz Korycki, Micha³ Polañski,\nMagda Baniukiewicz and Ewelina Kawecka.\n\n"
		    			+ "Nokia Wroc³aw representatives:\nMateusz Jaworski and Tomasz Drwiêga."
		    			+ "\n\nJune 2015";
				
				ImageIcon img = new ImageIcon("src/main/resources/images/ogr_icon_48.png");
				
				JOptionPane.showMessageDialog(mainWindow,
						aboutText,
					    "About",
					    JOptionPane.INFORMATION_MESSAGE,
					    img);
			}
		};
	}
	
	public ActionListener getRenameListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Item item = getMApp().getCurrentItem();
				
				if (item == null) {
					JOptionPane.showMessageDialog(mainWindow,
						"Cannot do rename operation - no item was selected.",
						"Friendly message", JOptionPane.INFORMATION_MESSAGE);
				}
				else {
					String newName = JOptionPane.showInputDialog(null, "Set new name:", getMApp().getCurrentItem().getName());
					if (newName != null && !newName.equals("")) {
						item.rename(newName);
					}	
				}
				
			}
		};
	}
	
	public ActionListener getDeleteListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Item item = getMApp().getCurrentItem();
				if (item == null) return;
				int res = JOptionPane.showConfirmDialog(mainWindow, "Do you want to delete this image and graph?");
				if (res == JOptionPane.YES_OPTION) {
					int index = getMApp().getCurrentItemIdx();
					getMApp().getItemListModel().removeElementAt(index);
				}
				getMApp().getMainPanel().resetView();
			}
		};
	}
	
	public ActionListener getSaveListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Item item = getMApp().getCurrentItem();
				if(item == null) return;
				int res = JOptionPane.showConfirmDialog(mainWindow, "Do you want to overwrite the graph file?");
				if(res == 0) {
					GraphFileConverter graphConverter = new GraphFileConverter();
					File newGraphFile = graphConverter.generateGraphJSON(
							getMApp().getMainPanel().getGraphPanel().getGraph(), item.getName()); // it overwrites the old graph			
					item.setGraphFile(newGraphFile);	// update handler
					JOptionPane.showMessageDialog(mainWindow, "Saved!");
				}
			}
		};
	}
	
	public ActionListener getOgrItListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainPanelController mpc = getMApp().getMainPanel().getController();
				mpc.cnvImage2Graph();				
			}
		};
	}
	
	public ActionListener getOcrItListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = {"ENG", "POL", "OGR"};
				String lang = (String)JOptionPane.showInputDialog(
				                    mainWindow,
				                    "Use language:\n",
				                    "OCR it!",
				                    JOptionPane.PLAIN_MESSAGE,
				                    null,
				                    options,
				                    "ENG");

				if (Arrays.asList(options).contains(lang)) {
					String ocr = OCR.ocr(getMApp().getCurrentImage().getAbsolutePath(), lang);
					JDialog dlg = new JDialog();
					dlg.setTitle("OCR result");
					JTextArea text = new JTextArea(ocr);
					dlg.add(text);
					dlg.pack();
					dlg.setLocationRelativeTo(mainWindow);
					dlg.setVisible(true);
				}
			}
		};
	}
	
	public ActionListener getTextColorListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = {"RED", "GREEN"};
				
				String res = (String) JOptionPane.showInputDialog(mainWindow, "Choose text color: ", "Settings", 
						JOptionPane.QUESTION_MESSAGE, null, options, getMApp().getColor().toString());
				
				if(res == null) return;
				if(res.equals("GREEN")) getMApp().setColor(FilterColor.GREEN);
				else if(res.equals("RED")) getMApp().setColor(FilterColor.RED);
			}
		};
	}
	
	public ActionListener getServerAddressListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JDialog dlg = new JDialog();
				dlg.setLayout(new GridLayout(2, 1));
				dlg.setTitle("Server adress");
				String current = ServerClient.getHost() + ':' + ServerClient.getPort();
				current = current.replace("http://", "");
				final JTextField input = new JTextField(current);
				JButton accept = new JButton("OK");
				dlg.add(input);
				dlg.add(accept);
				dlg.pack();
				dlg.setLocationRelativeTo(mainWindow);
				dlg.setVisible(true);
				
				accept.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						String answer = input.getText();
						int separator = answer.indexOf(':');
						String newHost = answer.substring(0, separator);
						String newPort = answer.substring(separator+1);
						ServerClient.setHost("http://" + newHost);
						ServerClient.setPort(newPort);
						dlg.setVisible(false);
					}
				});
			}
		};
	}
	
	private File showLoadFileDialog() {
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"JPEG (*.jpg)", "jpg");
		fileChooser.setFileFilter(filter);

		int userSelection = fileChooser.showOpenDialog(mainWindow);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		} else
			return null;
	}
}
