pragma solidity ^0.4.24;

contract Financial{
    //收据状态：有效、完成、逾期（未使用）
    enum Status{ Active, Accomplished, Overdue }
    
    struct Company {
        string name;            //公司名
        uint property;          //公司资产
        uint credibility;       //信誉度，0为不可信，1为需评估，2为可信
        bool isVaild;           //公司是否被注册
        mapping (address => Receipt[]) order;        //公司拥有的收据
    }
    
    struct Receipt{
        string payer;       //付款方
        string payee;       //收款方
        uint amount;        //金额
        uint credibility;   //信誉度
        Status status;      //应收账款状态
    }
    
    address public issuer;          //管理员公钥
    address[] public company;       //所有注册公司的地址
    mapping (address => Company) public roster;          //根据公钥对应公司

    event Transaction(string from, string to, uint amount);     //from: 付款方  to: 收款方  amount: 收据金额
    event Transfer(string from, string to, string next_from, uint amount);      //from: 转让前的付款方  to: 收款方  next_from: 转让后的付款方  amount: 收据金额
    event Financing(string from, uint amount, uint flag);       //from: 融资方  amount: 融资后资产  flag: 融资结果，0为失败，1为高信用企业融资，2为无高信用企业融资成功
    event Settlement(string from, string to, uint amount, uint property);       //from: 付款方  to: 收款方  amount: 还款金额  property: 还款后企业所剩资产
    
    constructor(){
        issuer = msg.sender;
        //银行作为特殊的企业
        roster[issuer] = Company("bank", 0, 3, true);
    }
    
    //注册公司和更改公司信息（增加资产和改变信誉度），返回值0表示失败，1表示注册，2表示已被注册
    function issue(address addr, string name, uint property, uint level) public returns(uint){ 
        //不是管理员账户
        if (msg.sender != issuer) 
            return 0; 
            
        //该公钥未被其他公司注册
        if(roster[addr].isVaild == false){
            company.push(addr);
            roster[addr] = Company(name, property, level, true);
            return 1;
        }
        else{
            return 2;
        }
    }
    
    function isExist(address addr) public returns(bool){
        if(roster[addr].isVaild == false)
            return false;
        else
            return true;
    }
    
    //生成收据的函数
    function generateReceipt(address sender, address receiver, uint amount) private{
        roster[receiver].order[sender].push(Receipt(roster[sender].name, roster[receiver].name, amount, roster[sender].credibility, Status.Active));
    }
    
    //公司之间交易
    function transaction(address receiver, uint amount) public{
        //收款公司未注册
        if(roster[receiver].isVaild == false)
            return;

        //生成订单    
        generateReceipt(msg.sender, receiver, amount);
        emit Transaction(roster[msg.sender].name, roster[receiver].name, amount);
    }
    
    //转让收据，有三种情况，优先用本公司拥有的收款公司的收据来抵消，不够再将高信誉度的公司的收据整合起来交给对方，还是不够两公司之间就生成收据
    function transfer(address receiver, uint amount) public{
        //收款公司未注册
        if(roster[receiver].isVaild == false)
            return;
        
        uint i;
        uint j;
        uint sum = 0;       //总额
        uint used = 0;      //
        
        //优先转让对方的收据
        for(i = 0; i < roster[msg.sender].order[receiver].length; i++){
            if(roster[msg.sender].order[receiver][i].status == Status.Active){
                sum += roster[msg.sender].order[receiver][i].amount;
                
                //第一种情况当前总额超过转让收据金额
                if(sum >= amount){
                    roster[msg.sender].order[receiver][i].amount -= (sum - amount);
                    emit Transfer(roster[msg.sender].name, roster[receiver].name, roster[receiver].name, sum - amount);
                    return;
                }
                else{       //未超过则将当前收据设为完成，相当于对方已经还款
                    roster[msg.sender].order[receiver][i].status = Status.Accomplished;
                    emit Transfer(roster[msg.sender].name, roster[receiver].name, roster[receiver].name, roster[msg.sender].order[receiver][i].amount);
                }
            }
        }
        
        //转让拥有的高信誉度公司的收据
        used = sum;         //第一种情况已经使用的总额
        //遍历寻找所有高信誉公司
        for(i = 0; i < company.length; i++){
            if(roster[company[i]].credibility == 2){
                sum = 0;

                //遍历转让方拥有的该公司所有的收据
                for(j = 0; j < roster[msg.sender].order[company[i]].length; j++){
                    if(roster[msg.sender].order[company[i]][j].status == Status.Active){
                        sum += roster[msg.sender].order[company[i]][j].amount;
                        //超过所需转让金额数则生成收据
                        if(used + sum >= amount){
                            roster[msg.sender].order[company[i]][j].amount -= (used + sum - amount);
                            generateReceipt(company[i], receiver, amount - used);
                            emit Transfer(roster[msg.sender].name, roster[receiver].name, roster[company[i]].name, amount - used);
                            return;
                        }
                        else
                            roster[msg.sender].order[company[i]][j].status = Status.Accomplished;
                    }
                }
                used += sum;
                
                //如果当前一家公司的总额不够，则先将其打包为一份收据转让给收款方
                generateReceipt(company[i], receiver, sum);
                emit Transfer(roster[msg.sender].name, roster[receiver].name, roster[company[i]].name, sum);
            }
        }
        
        //前两种情况无法达到要求金额，则转让方自己生成收据给对方
        generateReceipt(msg.sender, receiver, amount - used);
        emit Transfer(roster[msg.sender].name, roster[receiver].name, roster[msg.sender].name, amount - used);
    }
    
    //融资，高信誉度公司可以任意融资，非高信誉度公司根据所持有高信誉度公司的收据来决定融资的额度
    function financing(uint amount) public{
        uint i;
        uint j;
        
        //高信誉度公司可以任意融资
        if(roster[msg.sender].credibility == 2){
            generateReceipt(msg.sender, issuer, amount);
            roster[msg.sender].property += amount;
            emit Financing(roster[msg.sender].name, roster[msg.sender].property, 1);
        }
        else{
            uint used = 0;
            uint sum = 0;
            
            //已经使用的融资额度
            for(i = 0; i < roster[issuer].order[msg.sender].length; i++){
                if(roster[issuer].order[msg.sender][i].status == Status.Active)
                    used += roster[issuer].order[msg.sender][i].amount;
            }

            //计算剩余融资额度是否超过所需金额，如果不超过，则失败   
            for(i = 0; i < company.length; i++){
                if(roster[company[i]].credibility == 2){
                    for(j = 0; j < roster[msg.sender].order[company[i]].length; j++){
                        if(roster[msg.sender].order[company[i]][j].status == Status.Active){
                            sum += roster[msg.sender].order[company[i]][j].amount;
                            if(used + amount <= sum){
                                generateReceipt(msg.sender, issuer, amount);
                                roster[msg.sender].property += amount;
                                emit Financing(roster[msg.sender].name, roster[msg.sender].property, 2);
                                return;
                            }
                        }
                    }
                }
            }
            emit Financing(roster[msg.sender].name, roster[msg.sender].property, 0);
        }
    }
    
    //结账，参数为欲偿还公司的地址，根据收据创建先后顺序自动还款，直到剩余资产无法还款
    function settlement(address receiver) public{
        //收款公司未注册
        if(roster[receiver].isVaild == false)
            return;
        
        for(uint i = 0; i < roster[receiver].order[msg.sender].length; i++){
            if(roster[receiver].order[msg.sender][i].status == Status.Active){
                //资产足够偿还收据金额
                if(roster[msg.sender].property >= roster[receiver].order[msg.sender][i].amount){
                    roster[msg.sender].property -= roster[receiver].order[msg.sender][i].amount;
                    roster[receiver].property += roster[receiver].order[msg.sender][i].amount;
                    roster[receiver].order[msg.sender][i].status = Status.Accomplished;
                    emit Settlement(roster[msg.sender].name, roster[receiver].name, roster[receiver].order[msg.sender][i].amount, roster[msg.sender].property);
                }
                else
                    break;
            }
        }
    }
}