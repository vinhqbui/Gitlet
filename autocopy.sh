javac gitlet/*.java
rm -rf /Users/vinhbui/proj3-testing
mkdir /Users/vinhbui/proj3-testing
mkdir /Users/vinhbui/proj3-testing/gitlet
cp gitlet/*.class /Users/vinhbui/proj3-testing/gitlet
cp ~/samples/* /Users/vinhbui/proj3-testing
cd /Users/vinhbui/proj3-testing
java gitlet.Main init
java gitlet.Main status
