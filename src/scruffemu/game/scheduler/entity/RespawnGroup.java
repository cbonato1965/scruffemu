package scruffemu.game.scheduler.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import scruffemu.area.map.GameMap;
import scruffemu.common.Formulas;
import scruffemu.game.World;
import scruffemu.object.GameObject;
import scruffemu.utility.Pair;

public class RespawnGroup implements Runnable
{
  private final GameMap map;
  private final int cell;
  private final long lastTime;
  private volatile boolean exit=false;

  public RespawnGroup(GameMap map, int cell, long lastTime)
  {
    this.map=map;
    this.cell=cell;
    this.lastTime=lastTime;
  }

  public int getCell()
  {
    return cell;
  }

  public long getLastTime()
  {
    return lastTime;
  }

  public GameMap getMap()
  {
    return map;
  }

  public static final Map<String, Pair<ArrayList<GameObject>, Integer>> fixMobGroupObjects=new HashMap<>();

  @Override
  public void run()
  {
    long random=Formulas.getRandomValue(getMap().getMinRespawnTime(),getMap().getMaxRespawnTime());
    while(!exit)
    {
      try
      {
        long time=System.currentTimeMillis();
        if(this.cell!=-1) //is fixed group
        {
          if(World.world.getGroupFix(getMap().getId(),getCell())!=null)
          {
            Map<String, String> data=World.world.getGroupFix(getMap().getId(),getCell());
            if(time-this.lastTime>Long.parseLong(data.get("timer")))
            {
              getMap().addStaticGroup(getCell(),data.get("groupData"),true);
              this.stop();
              break;
            }
          }
          else if(World.world.getRandomGroupFix(getMap().getId(),getCell())!=null) //is random fixed group
          {
            Map<String, String> data2=World.world.getRandomGroupFix(getMap().getId(),getCell());
            if(data2.size()!=0)
            {
              if(time-this.lastTime>Long.parseLong(data2.get("timer")))
              {
                getMap().addRandomStaticGroup(getCell(),data2.get("groupData"),true);
                this.stop();
                break;
              }
            }
          }
        }
        else if(time-this.lastTime>random) //is normal group with timer expired
        {
          getMap().spawnGroup(-1,1,true,-1);
          this.stop();
          break;
        }
        Thread.sleep(500);
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
        this.stop();
      }
    }
  }

  public void stop()
  {
    exit=true;
  }
}