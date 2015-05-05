package spim.process.fusion.weights;

import net.imglib2.AbstractLocalizableInt;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;

public class TransformedInterpolatedRealRandomAccess< T > extends AbstractLocalizableInt implements RandomAccess< T >
{
	final RealRandomAccessible< T > realRandomAccessible;
	final RealRandomAccess< T > realRandomAccess;
	final Interval transformedInterval;
	final AffineTransform3D transform;
	final int[] offset;
	final int imgSizeX, imgSizeY, imgSizeZ;
	final T zero;

	/*
	 * Inverse coefficients of the transformation matrix
	 */
	final double i00, i01, i02, i03, i10, i11, i12, i13, i20, i21, i22, i23;

	final float[] tmp;

	public TransformedInterpolatedRealRandomAccess(
			final RealRandomAccessible< T > realRandomAccessible,
			final T zero,
			final Interval transformedInterval,
			final AffineTransform3D transform,
			final int[] offset )
	{
		super( realRandomAccessible.numDimensions() );

		this.transformedInterval = transformedInterval;
		this.zero = zero;
		this.realRandomAccessible = realRandomAccessible;
		this.transform = transform;
		this.offset = new int[ offset.length ];

		for ( int d = 0; d < n; ++d )
			this.position[ d ] = this.offset[ d ] = offset[ d ];

		this.realRandomAccess = realRandomAccessible.realRandomAccess();

		final double[] imatrix = transform.inverse().getRowPackedCopy();

		this.i00 = imatrix[ 0 ];
		this.i01 = imatrix[ 1 ];
		this.i02 = imatrix[ 2 ];
		this.i03 = imatrix[ 3 ];

		this.i10 = imatrix[ 4 ];
		this.i11 = imatrix[ 5 ];
		this.i12 = imatrix[ 6 ];
		this.i13 = imatrix[ 7 ];

		this.i20 = imatrix[ 8 ];
		this.i21 = imatrix[ 9 ];
		this.i22 = imatrix[ 10 ];
		this.i23 = imatrix[ 11 ];

		this.imgSizeX = (int)transformedInterval.dimension( 0 );
		this.imgSizeY = (int)transformedInterval.dimension( 1 );
		this.imgSizeZ = (int)transformedInterval.dimension( 2 );

		this.tmp = new float[ n ];
	}

	@Override
	public T get()
	{
		if ( applyInverseIntersects( i00, i01, i02, i03, i10, i11, i12, i13, i20, i21, i22, i23, imgSizeX, imgSizeY, imgSizeZ, tmp, position ) )
		{
			realRandomAccess.setPosition( tmp );
			return realRandomAccess.get();
		}
		else
		{
			return zero;
		}
	}

	private static final boolean applyInverseIntersects(
			final double i00, final double i01, final double i02, final double i03,
			final double i10, final double i11, final double i12, final double i13,
			final double i20, final double i21, final double i22, final double i23,
			final int imgSizeX, final int imgSizeY, final int imgSizeZ,
			final float[] source,
			final int[] target )
	{
		final double t0 = (double)target[ 0 ];
		final double t1 = (double)target[ 1 ];
		final double t2 = (double)target[ 2 ];

		final double s0 = t0 * i00 + t1 * i01 + t2 * i02 + i03;
		final double s1 = t0 * i10 + t1 * i11 + t2 * i12 + i13;
		final double s2 = t0 * i20 + t1 * i21 + t2 * i22 + i23;

		if ( intersects3d( s0, s1, s2, imgSizeX, imgSizeY, imgSizeZ ) )
		{
			source[ 0 ] = (float)s0;
			source[ 1 ] = (float)s1;
			source[ 2 ] = (float)s2;

			return true;
		}
		else
		{
			return false;
		}
	}

	private static final boolean intersects3d( final double x, final double y, final double z, final int sx, final int sy, final int sz )
	{
		if ( x >= 0 && y >= 0 && z >= 0 && x < sx && y < sy && z < sz )
			return true;
		else
			return false;
	}

	@Override
	public void fwd( final int d ) { ++this.position[ d ]; }

	@Override
	public void bck( final int d ) { --this.position[ d ]; }

	@Override
	public void move( final int distance, final int d ) { this.position[ d ] += distance; }

	@Override
	public void move( final long distance, final int d ) { this.position[ d ] += (int)distance; }

	@Override
	public void move( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			this.position[ d ] += localizable.getIntPosition( d );
	}

	@Override
	public void move( final int[] distance )
	{
		for ( int d = 0; d < n; ++d )
			this.position[ d ] += distance[ d ];
	}

	@Override
	public void move( final long[] distance )
	{
		for ( int d = 0; d < n; ++d )
			this.position[ d ] += (int)distance[ d ];
	}

	@Override
	public void setPosition( final Localizable localizable )
	{
		for ( int d = 0; d < n; ++d )
			this.position[ d ] = localizable.getIntPosition( d ) + offset[ d ];
	}

	@Override
	public void setPosition( final int[] position )
	{
		for ( int d = 0; d < n; ++d )
			this.position[ d ] = position[ d ] + offset[ d ];
	}

	@Override
	public void setPosition( final long[] position )
	{
		for ( int d = 0; d < n; ++d )
			this.position[ d ] = (int)position[ d ] + offset[ d ];
	}

	@Override
	public void setPosition( final int position, final int d ) { this.position[ d ] = position + offset[ d ]; }

	@Override
	public void setPosition( final long position, final int d ) { this.position[ d ] = (int)position + offset[ d ]; }

	@Override
	public TransformedInterpolatedRealRandomAccess< T > copy() { return new TransformedInterpolatedRealRandomAccess< T >( realRandomAccessible, zero, transformedInterval, transform, offset ); }

	@Override
	public TransformedInterpolatedRealRandomAccess<T> copyRandomAccess() { return copy(); }
}