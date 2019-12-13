# BlockChain_Financial
## 运行环境
Ubuntu16.04 + Eclipse + JDK11

## 文件结构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191213134914346.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rpb3NtYWlfa2luZ3Nv,size_16,color_FFFFFF,t_70)
代码实现主要都在src/main/java中
- org.fisco.bcos.financial.client：实现前端调用的api
- org.fisco.bcos.financial.contract：编译成java文件的合约
- org.fisco.bcos.financial.ui：UI界面

## 运行方法
运行org.fisco.bcos.financial.ui中的Starter.java文件

## 实际操作
需要先进行银行注册，因为设想情景是一个银行对应一个合约，所以在注册银行同时会自动部署合约，注册成功后再返回主界面，方可注册企业
