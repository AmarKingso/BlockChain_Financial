package org.fisco.bcos.financial.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.awt.event.ActionEvent;
import java.awt.Color;

import org.fisco.bcos.financial.client.*;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;

public class SignUp2 extends JFrame implements ActionListener{
	private JPanel contentPane;
	private JTextField textField1;
	private JTextField textField2;
	private JTextField textField3;
	private JTextField textField4;
	private JLabel lblAddr;
	private JLabel lblName;
	private JLabel lblProperty;
	private JLabel lblCredit;
	private JLabel lblSignup;
	private JLabel lblMsg;

	/**
	 * Create the frame.
	 */
	public SignUp2() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 680, 430);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField1 = new JTextField();
		textField1.setFont(new Font("Dialog", Font.PLAIN, 16));
		textField1.setBounds(160, 80, 360, 40);
		contentPane.add(textField1);
		textField1.setColumns(10);
		
		textField2 = new JTextField();
		textField2.setFont(new Font("Dialog", Font.PLAIN, 16));
		textField2.setColumns(10);
		textField2.setBounds(160, 132, 360, 40);
		contentPane.add(textField2);
		
		textField3 = new JTextField();
		textField3.setFont(new Font("Dialog", Font.PLAIN, 16));
		textField3.setColumns(10);
		textField3.setBounds(160, 184, 360, 40);
		contentPane.add(textField3);
		
		textField4 = new JTextField();
		textField4.setFont(new Font("Dialog", Font.PLAIN, 16));
		textField4.setColumns(10);
		textField4.setBounds(160, 236, 360, 40);
		contentPane.add(textField4);
		
		lblAddr = new JLabel("企业私钥");
		lblAddr.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddr.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblAddr.setBounds(60, 80, 100, 40);
		contentPane.add(lblAddr);
		
		lblName = new JLabel("企业名称");
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblName.setBounds(60, 132, 100, 40);
		contentPane.add(lblName);
		
		lblProperty = new JLabel("企业资产");
		lblProperty.setHorizontalAlignment(SwingConstants.CENTER);
		lblProperty.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblProperty.setBounds(60, 184, 100, 40);
		contentPane.add(lblProperty);
		
		lblCredit = new JLabel("企业信誉度");
		lblCredit.setHorizontalAlignment(SwingConstants.CENTER);
		lblCredit.setFont(new Font("Dialog", Font.PLAIN, 16));
		lblCredit.setBounds(60, 236, 100, 40);
		contentPane.add(lblCredit);
		
		lblSignup = new JLabel("企业注册");
		lblSignup.setHorizontalAlignment(SwingConstants.CENTER);
		lblSignup.setFont(new Font("Dialog", Font.BOLD, 28));
		lblSignup.setBounds(265, 12, 150, 40);
		contentPane.add(lblSignup);
		
		lblMsg = new JLabel("");
		lblMsg.setFont(new Font("DejaVu Sans Light", Font.BOLD, 12));
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		lblMsg.setBounds(0, 354, 680, 15);
		contentPane.add(lblMsg);
		
		JButton btnOkay = new JButton("确认");
		btnOkay.addActionListener(this);
		btnOkay.setFont(new Font("Dialog", Font.BOLD, 14));
		btnOkay.setBackground(new Color(102, 204, 0));
		btnOkay.setForeground(new Color(255, 255, 255));
		btnOkay.setBounds(190, 310, 100, 30);
		contentPane.add(btnOkay);
		
		JButton btnCancel = new JButton("返回");
		btnCancel.addActionListener(this);
		btnCancel.setForeground(new Color(255, 255, 255));
		btnCancel.setFont(new Font("Dialog", Font.BOLD, 14));
		btnCancel.setBackground(new Color(204, 0, 0));
		btnCancel.setBounds(390, 310, 100, 30);
		contentPane.add(btnCancel);
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getActionCommand() == "返回") {
			new SignIn();
			this.dispose();
		}
		else {
			FinancialClient admin = new FinancialClient();
			try {
				admin.initialize(FinancialClient.getAdminKey());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Sign up failed, error message is  " + e.getMessage());
			}
			String addr = GenCredential.create(textField1.getText()).getAddress();		//私钥对应的地址
			boolean ret = admin.issueAccount(addr, textField2.getText(), new BigInteger(textField3.getText()), new BigInteger(textField4.getText()));
			if(ret == false) {
				lblMsg.setText("The address is occupied");
			}
			else {
				System.out.printf("name: %s, address: %s\n", textField2.getText(), addr);
				FinancialClient client = new FinancialClient();
				try {
					client.initialize(textField1.getText());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Sign in failed, error message is  " + e.getMessage());
				}
				new MainMenu(client);
				this.dispose();
			}
		}
	}
}
