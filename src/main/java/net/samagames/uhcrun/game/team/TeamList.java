package net.samagames.uhcrun.game.team;

import java.util.*;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class TeamList implements List<Team>
{

    private final ArrayList<Team> internalList;

    public TeamList()
    {
        this.internalList = new ArrayList<>();
    }

    @Override
    public int size()
    {
        return internalList.size();
    }

    @Override
    public boolean isEmpty()
    {
        return internalList.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return internalList.contains(o);
    }

    @Override
    public Iterator<Team> iterator()
    {
        return internalList.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return internalList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        //noinspection SuspiciousToArrayCall
        return internalList.toArray(a);
    }

    @Override
    public boolean add(Team team)
    {
        return internalList.add(team);
    }

    @Override
    public boolean remove(Object o)
    {
        return internalList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return internalList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Team> c)
    {
        return internalList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Team> c)
    {
        return internalList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return internalList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return internalList.retainAll(c);
    }

    @Override
    public void clear()
    {
        internalList.clear();
    }

    @Override
    public Team get(int index)
    {
        return internalList.get(index);
    }

    @Override
    public Team set(int index, Team element)
    {
        return internalList.set(index, element);
    }

    @Override
    public void add(int index, Team element)
    {
        internalList.add(index, element);
    }

    @Override
    public Team remove(int index)
    {
        return internalList.remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return internalList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return internalList.lastIndexOf(o);
    }

    @Override
    public ListIterator<Team> listIterator()
    {
        return internalList.listIterator();
    }

    @Override
    public ListIterator<Team> listIterator(int index)
    {
        return internalList.listIterator(index);
    }

    @Override
    public List<Team> subList(int fromIndex, int toIndex)
    {
        return internalList.subList(fromIndex, toIndex);
    }


    public Team getTeam(UUID player)
    {
        for (Team team : internalList)
        {
            if (team.hasPlayer(player))
            {
                return team;
            }
        }
        return null;
    }
}
