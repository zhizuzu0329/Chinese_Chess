package chinese_chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;

/**
 * {@code chessboard} 棋盘类：<br/>
 * 定义一个数组存储32个棋子对象。二维数组map保存了当前棋子的布局。<br/>
 * map[x][y]=i表示棋盘第x行y列是棋子i,等于-1说明此处为空。<br/>
 * 声明arrayList<node>对象list用于保存每步棋的信息.<br/>
 */

public class chessboard  {
    public static final  int RED_PLAYER =1;
    public static final  int BLACK_PLAYER =0;
    public chess[] chess = new chess[32];
    public int[][] map = new int[9][8];
    public Image bufferImage;   //？？？？？？？还未用到
    private chess  firstChess = null;
    private chess  secondChess = null;
    private boolean isFirstClick = true;//是否第一次点击
    private  int x1,y1,x2,y2; //保存第一次第二次选中的坐标
    private int tempX,tempY;      //不太懂？？？？？？，可能是一个临时变量
    private boolean isMyTurn = true;//是否自己执子
    public int localPlayer = RED_PLAYER;//当前执子方
    private String message = "";//提示信息
    private boolean flag = false;   //？？？？？不知道用处
    private int otherPort=3003; //外部端口
    private int myPort=3004;  //本地端口
    public arrayList<node> list = new arrayList<node>();//存储棋盘
    private String ip = "127.0.0.3"; //目标ip
    /**
     * 对棋盘进行初始化，全部为-1，表示没有棋子
     */
    private void initMap(){
        int i,j;
        for(i=0;i<10;i++) {
            for (j = 0; j < 9; j++) {
                map[i][j] = -1;
            }
        }
    }



public chessboard(){
    initMap() ;
    message = "程序处于等待联机状态！";
    addMouseListener(new MouseAdapter() {       //不太懂
        @Override
        public void  mouseClicked(MouseEvent e){
            if(isMyTurn == false){
                message = "现在是对方走棋";
                repaint();
                return;
            }
            else{
            selectedchess(e);
            repaint();
            }
        }
    }


    private void selectedchess(MouseEvent e){
        int index1, index2;
        if (isFirstClick) {
            firstChess = analyse(e.getX(), e.getY());
            x1 = tempX;
            y1 = tempY;
            if (firstChess != null) {
                if (firstChess.player != localPlayer) {
                    message = "点成对方棋子了";
                    return ;
                }
                else {
                    isFirstClick = false;
                }
            }
        } else {
            secondChess = analyse(e.getX(), e.getY());
            x2 = tempX;
            y2 = tempY;
            if (secondChess != null) {   //如果第二次选中了棋子
                if (secondChess.play == localPlayer) {//第二次选中了我方棋子，对第一次点击的棋子进行更换
                    firstChess = secondChess;
                    x1 = tempX;
                    y1 = tempY;
                    secondChess = null;
                    return;
                }
            }
            if (secondChess == null) {  //点击空处，判断是否可以走棋
                if (isAbleToMove(firstChess, x2, y2)) {
                    index1 = map[x1][y1];
                    map[x1][y1] = -1;
                    map[x2][y2] = index1;
                    chess[index1].setPoint(x2, y2);
                    send("move" + "|" + index1 + "|" + (9 - x2) + "|" + (8 - y2) + "|" + (9 - x1) + "|" + (8 - y1) + "|" + "-1");  ///????，推测是UDP协议发送棋子变更信息
                    list.add(new node(index1, x2, y2, x1, y1, -1));//存储我方下棋信息
                    isFirstClick = true;
                    repaint();
                    setMyTurn(false);  //还没有写
                } else {
                    message = "不符合走棋规则";
                }
                return;
            }
            if (secondChess != null && isAbleToMove(firstChess, x2, y2)) {  //是否能吃棋
                isFirstClick = true;
                index1 = map[x1][y1];
                index2 = map[x2][y2];
                map[x1][y1] = -1;
                map[x2][y2] = index1;
                chess[index1].setPoint(x2, y2);
                chess[index2] = null;
                repaint();
                send("move" + "|" + index1 + "|" + (9 - x2) + "|" + (8 - y2) + "|" + (9 - x1) + "|" + (8 - y1) + "|" + index2 + "|");
                list.add(new node(index1, x2, y2, x1, y1, index2));
                if (index2 == 0) {      //将被吃掉
                    message = "红方赢了";
                    JOptionPane.showConfirmDialog(null, "红方赢了", "提示", JOptionPane.DEFAULT_OPTION);
                    send("succ" + "|" + "红方赢了" + "|");
                    return;
                }
                if (index2 == 16) {      //帅被吃掉
                    message = "黑方赢了";
                    JOptionPane.showConfirmDialog(null, "黑方赢了", "提示", JOptionPane.DEFAULT_OPTION);
                    send("succ" + "|" + "黑方赢了" + "|");
                    return;
                }
                setMyTurn(false);
            } else {
                message = "不能吃子";
            }
        }//第二次点击完
    }
 }
}
