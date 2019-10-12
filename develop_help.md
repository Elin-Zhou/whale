## 升级版本号
### 进入项目的根目录执行
```
mvn versions:set -DnewVersion=XXXX
```
其中XXX表示需要的版本号

### 确认更改
```mvn versions:commit```


### 撤销修改
```mvn versions:revert```