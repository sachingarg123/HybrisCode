/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.woolies.webservices.rest.queues.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.woolies.webservices.rest.queues.UpdateQueue;


/**
 * Abstract implementation of {@link com.woolies.webservices.rest.queues.UpdateQueue} using {@link TreeMap} for storing
 * elements WARNING: this queue has limited capacity due to its in-memory nature
 */
public abstract class AbstractUpdateQueue<T> extends TreeMap<Long, T> implements UpdateQueue<T> //NOSONAR
{
	private int maxCapacity = 1000;

	/**
	 * This method is used to get items from queue
	 */
	@Override
	public List<T> getItems()
	{
		return Lists.newArrayList(values());
	}

	/**
	 * This method is used to get items for the given date return give the list of newer items
	 */
	@Override
	public List<T> getItems(final Date newerThan)
	{
		return Lists.newArrayList(tailMap(Long.valueOf(newerThan.getTime())).values());
	}

	/**
	 * This method is used to add item
	 */
	@Override
	public void addItem(final T item)
	{
		if (size() < maxCapacity)
		{
			Long timeKey = getTimeKey(item);
			while (containsKey(timeKey))
			{
				timeKey = Long.valueOf(timeKey.longValue() + 1);
			}
			put(timeKey, item);
		}
	}

	/**
	 * This method is used to add items
	 */
	@Override
	public void addItems(final List<T> items)
	{
		for (final T item : items)
		{
			addItem(item);
		}
	}

	/**
	 * This method is used to remove older items and return all the new items
	 */
	@Override
	public void removeItems(final Date olderThan)
	{
		final SortedMap<Long, T> clone = (SortedMap<Long, T>) clone();
		final SortedMap<Long, T> newerThan = clone.tailMap(Long.valueOf(olderThan.getTime()));
		clear();
		putAll(newerThan);
	}

	/**
	 * This method is used to remove all the items from the queue
	 */
	@Override
	public void removeItems()
	{
		clear();
	}

	@Override
	public void removeItems(final Predicate<T> predicate)
	{
		final Iterator<T> it = values().iterator();
		while (it.hasNext())
		{
			if (predicate.apply(it.next()))
			{
				it.remove();
			}
		}
	}

	/**
	 * This method is used to get last item in the queue
	 */
	@Override
	public T getLastItem()
	{
		T ret = null;
		if (!isEmpty())
		{
			ret = lastEntry().getValue();
		}
		return ret;
	}

	/**
	 * This method is used to get the max capacity of the queue
	 * 
	 * @return maxCapacity
	 */
	public int getMaxCapacity()
	{
		return maxCapacity;
	}

	/**
	 * This method is used to set the max capacity of the queue
	 * 
	 * @param maxCapacity
	 */
	public void setMaxCapacity(final int maxCapacity)
	{
		this.maxCapacity = maxCapacity;
	}

	/**
	 * This method is used to get time key for the given item
	 * 
	 * @param item
	 * @return gives current time is milli seconds
	 */
	protected Long getTimeKey(@SuppressWarnings("unused") final T item) //NOSONAR
	{
		return Long.valueOf(System.currentTimeMillis());
	}

}
