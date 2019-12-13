package org.fisco.bcos.financial.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.fisco.bcos.financial.client.FinancialClient;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;

import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Color;

public class SignUp1 extends JFrame implements ActionListener{

	private JPanel contentPane;
	private JTextField textField;
	
	/**
	 * Create the frame.
	 */
	public SignUp1() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 680, 430);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setFont(new Font("Dialog", Font.PLAIN, 16));
		textField.setColumns(10);
		textField.setBounds(160, 148, 360, 40);
		contentPane.add(textField);
		
		JLabel lblAddr = new JLabel("企业私钥");
		lblAddr.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddr.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblAddr.setBounds(60, 148, 100, 40);
		contentPane.add(lblAddr);
		
		JLabel lblSignup = new JLabel("金融机构注册");
		lblSignup.setHorizontalAlignment(SwingConstants.CENTER);
		lblSignup.setFont(new Font("Dialog", Font.BOLD, 28));
		lblSignup.setBounds(240, 75, 200, 40);
		contentPane.add(lblSignup);
		
		JLabel lblMsg = new JLabel("");
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		lblMsg.setFont(new Font("DejaVu Sans Light", Font.BOLD, 12));
		lblMsg.setBounds(0, 308, 680, 15);
		contentPane.add(lblMsg);
		
		JButton btnOkay = new JButton("确认");
		btnOkay.addActionListener(this);
		btnOkay.setForeground(Color.WHITE);
		btnOkay.setFont(new Font("Dialog", Font.BOLD, 14));
		btnOkay.setBackground(new Color(102, 204, 0));
		btnOkay.setBounds(190, 238, 100, 30);
		contentPane.add(btnOkay);
		
		JButton btnCancel = new JButton("返回");
		btnCancel.addActionListener(this);
		btnCancel.setForeground(Color.WHITE);
		btnCancel.setFont(new Font("Dialog", Font.BOLD, 14));
		btnCancel.setBackground(new Color(204, 0, 0));
		btnCancel.setBounds(390, 238, 100, 30);
		contentPane.add(btnCancel);
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand() == "返回") {
			new SignIn();
			this.dispose();
		}
		else {
			FinancialClient client = new FinancialClient();
			try {
				client.initialize(textField.getText());
				client.setAdminKey(textField.getText());
				client.deployAssetAndRecordAddr();
				System.out.printf("name: bank, address: %s\n", GenCredential.create(textField.getText()).getAddress());
				new MainMenu(client);
				this.dispose();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Sign up failed, error message is  " + e.getMessage());
			}
		}
	}
}
