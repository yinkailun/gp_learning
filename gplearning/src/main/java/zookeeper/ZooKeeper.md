## 1.什么是zookeeper
- zk是一个分布式协调服务，设计初衷是为了将那些复杂的**分布式数据一致性问题**封装成简单的方便客户使用的框架。
- 可使用zk实现类似 发布订阅，负载均衡，命名服务、分布式协调通知，集群管理等功能。
- 最常用的场景是使用其作为服务注册者和消费者的注册中心（注册中心类似于Nacos,eureka和consul）
## 2.重要概念
- zk本身就是一个分布式程序(只要半数以上节点存活，zk就能正常服务)
- zk将数据保存在内存中，保证高吞吐和低延迟
- zk有临时节点，当创建临时节点的会话存在，临时节点就存在，当会话终结，临时节点被清除，持久节点是当这个节点ZNode被创建，除非主动删除，否则永久存在。
- 底层其实只提供两个功能
    - 管理用户程序提交数据
    - 为用户程序提交数据节点的监控服务
- session(会话)
    - 指zk服务端和客户端会话，在zk中，一个客户端连接是指客户端和服务端创建的TCP长连接
    - 通过这个连接，客户端可以向服务端发送心跳确保有效会话，还能向服务端发送请求接收响应，并接受服务端的watch监控通知。
    - sessionTimeout会话超时时间，一个会话断开连接，只要在sessionTimeOut时间内重新连接到zk的任意一台服务器，即可重新连接，之前会话依然有效。
    - 创建连接之前会给客户端分配唯一的sessionId,,该id全局唯一
- ZNode
    - 节点类型：
        * 机器节点:构成集群的机器
        * 数据节点:数据模型中的数据单元
    zk将所有数据存储在内存中，数据模型是树形结构(ZNode Tree),由斜杠(/)分割的路径，就是一个ZNode
- 版本
    - 每个ZNode,zk都会维护一个叫stat的数据结构，记录当前ZNode的三个版本，分别是dataversion(当前ZNode版本),cversion(子节点版本)和aclversion(ACL版本)   
- Watcher事件监听
    - zk允许用户在指定节点注册watcher并且在一些特定事件触发的时候将事件通知发送到客户端
- ACL(access control lists)策略进行权限控制
    - create:创建子节点权限
    - read:获取节点数据和子节点列表权限
    - write:更新节点数据权限
    - delete:删除子节点权限
    - 设置节点ACL权限
    create和delete都是针对子节点来控制的。
- ZAB协议和Paxos算法
    ....
## 3.从acid到CAP和BASE
- acid原子性，一致性隔离性(read uncommited/read commit/repeatable read /serializable)和持久性
- 经典分布式理论
    -  CAP：一个分布式系统不可能同时满足一致性(Consistency)，可用性(available),和分区容错性(partition tolerance),最多只能满足其中两条。
        - C：consistency:数据在多个节点上的一致性
        - A:available：服务的可用性，对用户请求，在有限时间内返回结果。
        - Partition tolerance：分区容错性：分布式系统在遇到网络分区故障(部分节点故障)，仍能对外提供服务
        - cap取舍
            * 放弃p,可将事务有关的数据放在一个节点上，但是就放弃了可扩展性
            * 放弃a,可能会导致服务不可用
            * 放弃c,放弃数据强一致性，保证数据最终的一致性。
            * 但是分布式系统p是基本需求，一般在a和c上做平衡。
    - BASE理论：Base available(基本可用) + soft state(软状态/弱状态) + Eventually consistency(最终一致性)
        - Base available:系统出现故障允许**损失部分功能**，但是不是不可用。(例如允许响应从0.5s到1-2s,或者大促期间，只保证核心功能可用，其他功能降级。)
        - soft state:弱状态/软状态：允许不同数据副本之间进行数据同步时存在延迟。
        - eventually consistency：最终一致性，指不同数据副本，经过一段时间之后，数据保持最终一致。
## 4.一致性协议
- 二阶段提交协议:2PC(two-phase commit)
    - 协调者(统一调度分布式节点的执行逻辑的组件)，参与者(各个节点)
    - 阶段一：提交事务请求，协调者向所有的参与者发送事务请求，，参与者执行事务操作，返回yes/no.,记录redo和undo信息
    - 阶段二：若协调者收到的都是yes,则向所有的参与者发送commit请求，否则发送rollback请求，参与者处理完成之后返回给协调者ack消息，参与者接收到所有的ack消息之后完成事务。
    - 最大问题：
        * 同步阻塞：在二阶段提交执行过程中，各个参与者都处于阻塞装填。
        * 单点问题：协调者出问题，就凉凉。
        * ...
- 三阶段提交协议:3PC
    - CanCommit:向参与者发送事务请求，确认是否可以执行事务，返回yes/no
    - PreCommit:若都是yes则发送precommit给参与者，执行事务，记录redo/undo.若失败则发送abort
    - docommit:协调者接受ack后,发送真正的docommit或者abort
    - 优点，cancommit降低了参与则阻塞范围。
    - 缺点：precommit时若出现网络分区，参与者仍会提交事务。则会出现数据不一致问题？？？
- paxos 算法
    - 前提，拜占庭将军问题中的信息不会被篡改。
    - 目标是最终有一个提案被选定，被选定后，进程最终也能获取到被选定的提案。
    - proposer(提案)，acceptor,learner
    - 详细过程...
## 5.ZAB协议
- 什么是zab协议
    - zk不是完全使用paxos算法，而是使用 zookeeper atomic broadcast(zk原子广播)协议
    - 所有请求必须由全局唯一的服务器来处理，该服务器被称为leader服务器，其他服务器被称为follower服务器。
    - leader将客户端事务转成事务proposal(提案)，将提案转给所有的follower服务器，若超过半数follower服务器正确反馈，则再次想所有的follower服务器发送commit消息，要求其将前一个proposal提交.
- 两种基本模式
    - 崩溃恢复:整个服务框架启动，或者leader出现网络中断，崩溃退出，重启等异常，进入恢复模式，重新选举新的leader,当集群中有过半的服务器和leader完成数据同步后，退出恢复模式。
    - 消息广播:原子广播，类似于2PC(2阶段提交)
        - 1.将请求生成对应的事务proposal，发送给集群其他机器，收集选票，和2pc不同,follower可以同意，也可以抛弃
        - 2.过半服务器返回ack，就会广播commit proposal请求，而不需要等待所有。
        - 3.消息顺序性：使用有FIFO特性的TCP协议。
    
 
        