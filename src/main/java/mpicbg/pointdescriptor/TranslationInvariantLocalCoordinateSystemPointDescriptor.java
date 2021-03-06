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
package mpicbg.pointdescriptor;

import java.util.ArrayList;

import mpicbg.models.Point;
import mpicbg.models.PointMatch;
import mpicbg.pointdescriptor.exception.NoSuitablePointsException;
import net.imglib2.RealLocalizable;

public class TranslationInvariantLocalCoordinateSystemPointDescriptor < P extends Point > extends AbstractPointDescriptor< P, TranslationInvariantLocalCoordinateSystemPointDescriptor<P> >
		implements RealLocalizable
{
	public double ax, ay, az, bx, by, bz;
	
	public TranslationInvariantLocalCoordinateSystemPointDescriptor( final P basisPoint, final P point1, final P point2 ) throws NoSuitablePointsException 
	{
		super( basisPoint, toList( point1, point2 ), null, null );
		
		if ( numDimensions != 3 )
			throw new NoSuitablePointsException( "LocalCoordinateSystemPointDescriptor does not support dim = " + numDimensions + ", only dim = 3 is valid." );

		buildLocalCoordinateSystem( descriptorPoints );
	}

	private static < P extends Point > ArrayList< P > toList( final P point1, final P point2 )
	{
		final ArrayList< P > list = new ArrayList<>();
		list.add( point1 );
		list.add( point2 );
		return list;
	}

	@Override
	public double descriptorDistance( final TranslationInvariantLocalCoordinateSystemPointDescriptor< P > pointDescriptor )
	{ 
		double difference = 0;

		difference += ( ax - pointDescriptor.ax ) * ( ax - pointDescriptor.ax );
		difference += ( ay - pointDescriptor.ay ) * ( ay - pointDescriptor.ay );
		difference += ( az - pointDescriptor.az ) * ( az - pointDescriptor.az );
		difference += ( bx - pointDescriptor.bx ) * ( bx - pointDescriptor.bx );
		difference += ( by - pointDescriptor.by ) * ( by - pointDescriptor.by );
		difference += ( bz - pointDescriptor.bz ) * ( bz - pointDescriptor.bz );

		return difference;// / 3.0;	
	}
	
	/**
	 * Not necessary as the main matching method is overwritten
	 */
	@Override
	public Object fitMatches( final ArrayList<PointMatch> matches )  { return null; }
	
	public void buildLocalCoordinateSystem( final ArrayList< LinkedPoint< P > > neighbors )
	{
		this.ax = neighbors.get( 0 ).getL()[ 0 ];
		this.ay = neighbors.get( 0 ).getL()[ 1 ];
		this.az = neighbors.get( 0 ).getL()[ 2 ];

		this.bx = neighbors.get( 1 ).getL()[ 0 ];
		this.by = neighbors.get( 1 ).getL()[ 1 ];
		this.bz = neighbors.get( 1 ).getL()[ 2 ];
	}

	@Override
	public int numDimensions() { return 6; }

	@Override
	public boolean resetWorldCoordinatesAfterMatching() { return true; }

	@Override
	public boolean useWorldCoordinatesForDescriptorBuildUp() { return false; }

	@Override
	public void localize( final float[] position )
	{
		position[ 0 ] = (float)ax;
		position[ 1 ] = (float)ay;
		position[ 2 ] = (float)az;
		position[ 3 ] = (float)bx;
		position[ 4 ] = (float)by;
		position[ 5 ] = (float)bz;
	}

	@Override
	public void localize( final double[] position )
	{
		position[ 0 ] = ax;
		position[ 1 ] = ay;
		position[ 2 ] = az;
		position[ 3 ] = bx;
		position[ 4 ] = by;
		position[ 5 ] = bz;
	}

	@Override
	public double getDoublePosition( final int d )
	{
		if ( d == 0 )
			return ax;
		else if ( d == 1 )
			return ay;
		else if ( d == 2 )
			return az;
		else if ( d == 3 )
			return bx;
		else if ( d == 4 )
			return by;
		else
			return bz;
	}

	@Override
	public float getFloatPosition( final int d ) { return (float)getDoublePosition( d ); }
}
