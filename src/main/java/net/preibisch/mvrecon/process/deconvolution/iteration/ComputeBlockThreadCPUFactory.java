/*-
 * #%L
 * Software for the reconstruction of multi-view microscopic acquisitions
 * like Selective Plane Illumination Microscopy (SPIM) Data.
 * %%
 * Copyright (C) 2012 - 2017 Multiview Reconstruction developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package net.preibisch.mvrecon.process.deconvolution.iteration;

import java.util.concurrent.ExecutorService;

import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.preibisch.mvrecon.process.deconvolution.MultiViewDeconvolution;

public class ComputeBlockThreadCPUFactory implements ComputeBlockThreadFactory
{
	final ExecutorService service;
	final float minValue;
	final float lambda;
	final int[] blockSize;
	final ImgFactory< FloatType > blockFactory;

	public ComputeBlockThreadCPUFactory(
			final ExecutorService service,
			final float minValue,
			final float lambda,
			final int[] blockSize,
			final ImgFactory< FloatType > blockFactory )
	{
		this.service = service;
		this.minValue = minValue;
		this.lambda = lambda;
		this.blockSize = blockSize.clone();
		this.blockFactory = blockFactory;
	}

	public ComputeBlockThreadCPUFactory(
			final ExecutorService service,
			final float lambda,
			final int[] blockSize,
			final ImgFactory< FloatType > blockFactory )
	{
		this.service = service;
		this.minValue = MultiViewDeconvolution.minValue;
		this.lambda = lambda;
		this.blockSize = blockSize.clone();
		this.blockFactory = blockFactory;
	}

	@Override
	public ComputeBlockThread create( final int id )
	{
		return new ComputeBlockThreadCPU( service, minValue, lambda, id, blockSize, blockFactory );
	}

	@Override
	public int numParallelBlocks() { return 1; }

	@Override
	public String toString()
	{
		return "CPU based";
	}
}
