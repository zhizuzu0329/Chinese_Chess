package chinese_chess;

import java.awt.Graphics; //导入包，用于创建几何图形。
import java.awt.Image;    //用于创建和修改图像。
import java.awt.Toolkit;  //一个抽象类，提供GUI底层的java访问。
import java.awt.image.ImageObserver; //接口，用于返回图像的高度、宽度、属性。
import javax.swing.JPanel; //JPanel是一般轻量级容器。

public class chess {
    public static final  int RED_PLAYER =1;       //红方玩家
    public static final  int BLACK_PLAYER =0;     //黑方玩家
    public int player;        //玩家
    public String typeName;   //棋子类型名
    public int x,y;           //网格地图对应二维数组的下标
    private Image chessImage;
    private int leftX=30,leftY=35;  //图像距离左边缘30，距离右边缘35
    public chess( int player,String typeName,int x,int y) {
        this.player = player;
        this.typeName = typeName;
        this.x = x;
        this.y = y;
        if (player == RED_PLAYER) {
            switch (typeName) {
                case "帅":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess8.png");
                    break;
                case "仕":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess8.png");
                    break;
                case "相":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess9.png");
                    break;
                case "马":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess10.png");
                    break;
                case "车":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess11.png");
                    break;
                case "炮":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess12.png");
                    break;
                case "兵":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess13.png");
                    break;
                default:
                    break;
            }
        } else {
            switch (typeName) {
                case "将":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess0.png");
                    break;
                case "士":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess1.png");
                    break;
                case "象":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess2.png");
                    break;
                case "马":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess3.png");
                    break;
                case "车":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess4.png");
                    break;
                case "炮":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess5.png");
                    break;
                case "卒":
                    chessImage = Toolkit.getDefaultToolkit().getImage("F:/idea_test/images/chess.png");
                    break;
                default:
                    ;
                    break;
            }
        }
    };
    public  void SetPoint(int x,int y){
        this.x=x;
        this.y=y;
    }
    public void ReversePoints(int x,int y){

    }
}


