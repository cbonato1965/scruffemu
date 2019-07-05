package scruffemu.fight.ia.type;

import scruffemu.fight.Fight;
import scruffemu.fight.Fighter;
import scruffemu.fight.ia.AbstractNeedSpell;
import scruffemu.fight.ia.util.Function;
import scruffemu.fight.spells.Spell;
import scruffemu.main.Config;

import java.util.Iterator;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA72 extends AbstractNeedSpell
{

  boolean hasSummons=false;
  Fighter summon=null;
  public IA72(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
      int time=100,maxPo=1;
      boolean action=false;
      Fighter ennemy=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);

      for(Spell.SortStats spellStats : this.highests)
        if(spellStats.getMaxPO()>maxPo)
          maxPo=spellStats.getMaxPO();

      Fighter L=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1); // pomax +1;
      Fighter C=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2); //2 = po min 1 + 1;

      if(maxPo==1)
        L=null;
      if(C!=null)
        if(C.isHide())
          C=null;
      if(L!=null)
        if(L.isHide())
          L=null;

      if(this.fighter.getCurPm(this.fight)>0&&L==null&&C==null)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,ennemy);
        if(value!=0)
        {
          time=value;
          action=true;
          L=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1); // pomax +1;
          C=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2); //2 = po min 1 + 1;
          if(maxPo==1)
            L=null;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().invocIfPossible(this.fight,this.fighter,this.invocations))
        {
          time=600;
          action=true;
        }
      }

      int percentPdv=(this.fighter.getPdv()*100)/this.fighter.getPdvMax();

      if(this.fighter.getCurPa(this.fight)>0&&!action&&percentPdv<50)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,true,50)!=0)
        {
          time=400;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().buffIfPossible(this.fight,this.fighter,this.fighter,this.buffs))
        {
          time=400;
          action=true;
        }
      }

      if(!hasSummons)
      {
        Iterator<Fighter> fightItt=this.fight.getFighters(this.fighter.getOtherTeam()).iterator();
        {
          while(fightItt.hasNext())
          {
            Fighter nextFighter=fightItt.next();
            if(nextFighter.isInvocation())
            {
              hasSummons=true;
              summon=nextFighter;
            }
          }
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&L!=null&&C==null&&!action&&hasSummons&&summon!=null)
      {
        int value=Function.getInstance().attackIfPossibleAll(this.fight,this.fighter,summon);
        if(value!=0)
        {
          time=value;
          action=true;
          hasSummons=false;
          summon=null;
        }
      } else if(this.fighter.getCurPa(this.fight)>0&&C!=null&&!action)
      {
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.cacs);
        if(value!=0)
        {
          time=value;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,false,80)!=0)
        {
          time=400;
          action=true;
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&!action)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,ennemy);
        if(value!=0)
          time=value;
      }

      if(this.fighter.getCurPa(this.fight)==0&&this.fighter.getCurPm(this.fight)==0)
        this.stop=true;
      addNext(this::decrementCount,time+Config.getInstance().AIDelay);
    } else
    {
      this.stop=true;
    }
  }
}