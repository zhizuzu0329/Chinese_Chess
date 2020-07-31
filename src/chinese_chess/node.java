package chinese_chess;
/**
 * {@code node} 自定义类：用于记录每步棋的信息.<br/>
 *  index 表示移动的棋子下标。<br/>
 *  x,y 表示棋子移动后位于（x.y）。<br/>
 *  oldX,oldY 表示棋子移动前位于（oldX.oldY）。<br/>
 *  eatChessIndex 表示被吃掉的棋子下标。<br/>
 */
public class node {
    int index;
    int x,y;
    int oldX,oldY;
    int eatChessIndex;
    public node(int index,int x,int y,int oldX,int oldY,int eatChessIndex){
        this.index=index;
        this.x=x;
        this.y=y;
        this.oldX=oldX;
        this.oldY=oldY;
        this.eatChessIndex=eatChessIndex;
    }
}


