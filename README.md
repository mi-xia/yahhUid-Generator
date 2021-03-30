# yahhUid-Generator
一款基于雪花算法的id生成器，参考百度uid编写，[原项目地址](https://github.com/baidu/uid-generator)

原本只是打算学习使用，但是使用过程中发现因原项目似乎不再维护，所以干脆进行移植，留作日常维护，如果有人使用了该组件发现任何问题，也欢迎提issue

但是再次声明：原创作团队为百度技术团队，本项目只是留作日常维护！！！

组件详细原理可参考 [原项目地址](https://github.com/baidu/uid-generator)

2021.03.30：封装了一层自动装配，可通过 uid.type=standard 或 uid.type=cached 配置决定使用标准模式还是缓存模式