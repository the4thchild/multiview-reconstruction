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
package net.preibisch.mvrecon.process.interestpointregistration.pairwise.methods.rgldm;

import java.util.ArrayList;
import java.util.List;

import net.preibisch.mvrecon.fiji.ImgLib2Temp.Pair;
import net.preibisch.mvrecon.fiji.spimdata.interestpoints.InterestPoint;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.MatcherPairwise;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.PairwiseResult;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.methods.ransac.RANSAC;
import net.preibisch.mvrecon.process.interestpointregistration.pairwise.methods.ransac.RANSACParameters;

import mpicbg.spim.mpicbg.PointMatchGeneric;

public class RGLDMPairwise< I extends InterestPoint > implements MatcherPairwise< I >
{
	final RANSACParameters rp;
	final RGLDMParameters dp;

	public RGLDMPairwise(
			final RANSACParameters rp,
			final RGLDMParameters dp  )
	{
		this.rp = rp;
		this.dp = dp;
	}

	@Override
	public PairwiseResult< I > match( final List< I > listAIn, final List< I > listBIn )
	{
		final PairwiseResult< I > result = new PairwiseResult< I >( true );

		final ArrayList< I > listA = new ArrayList< I >();
		final ArrayList< I > listB = new ArrayList< I >();

		for ( final I i : listAIn )
			listA.add( i );

		for ( final I i : listBIn )
			listB.add( i );

		if ( listA.size() < 4 || listB.size() < 4 )
		{
			result.setResult( System.currentTimeMillis(), "Not enough detections to match" );
			result.setCandidates( new ArrayList< PointMatchGeneric< I > >() );
			result.setInliers( new ArrayList< PointMatchGeneric< I > >(), Double.NaN );
			return result;
		}

		final RGLDMMatcher< I > matcher = new RGLDMMatcher< I >();
		final ArrayList< PointMatchGeneric< I > > candidates = matcher.extractCorrespondenceCandidates(
				listA,
				listB,
				dp.getNumNeighbors(),
				dp.getRedundancy(),
				dp.getRatioOfDistance(),
				dp.getDifferenceThreshold() );

		result.setCandidates( candidates );

		// compute ransac and remove inconsistent candidates
		final ArrayList< PointMatchGeneric< I > > inliers = new ArrayList<>();
	
		final Pair< String, Double > ransacResult = RANSAC.computeRANSAC( candidates, inliers, dp.getModel(), rp.getMaxEpsilon(), rp.getMinInlierRatio(), rp.getMinInlierFactor(), rp.getNumIterations() );
	
		result.setInliers( inliers, ransacResult.getB() );
	
		result.setResult( System.currentTimeMillis(), ransacResult.getA() );
		
		return result;
	}

	/**
	 * We only read the points, no reason to duplicate, RANSAC does its own duplication
	 */
	@Override
	public boolean requiresInterestPointDuplication() { return true; }
}
