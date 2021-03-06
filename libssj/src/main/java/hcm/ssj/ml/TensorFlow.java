/*
 * TensorFlowModel.java
 * Copyright (c) 2017
 * Authors: Ionut Damian, Michael Dietz, Frank Gaibler, Daniel Langerenken, Simon Flutura
 * *****************************************************
 * This file is part of the Social Signal Interpretation for Java (SSJ) framework
 * developed at the Lab for Human Centered Multimedia of the University of Augsburg.
 *
 * SSJ has been inspired by the SSI (http://openssi.net) framework. SSJ is not a
 * one-to-one port of SSI to Java, it is an approximation. Nor does SSJ pretend
 * to offer SSI's comprehensive functionality and performance (this is java after all).
 * Nevertheless, SSJ borrows a lot of programming patterns from SSI.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package hcm.ssj.ml;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.FloatBuffer;

import hcm.ssj.core.Cons;
import hcm.ssj.core.Log;
import hcm.ssj.core.stream.Stream;

/**
 * TensorFlow model.
 * Supports prediction using tensor flow's Android API.
 * Requires pre-trained frozen graph, e.g. using SSI.
 *
 * @author Vitaly Krumins
 */

public class TensorFlow extends Model
{
	private Graph modelGraph;
	private Session session;

	private int classNum;
	private String[] classNames;

	static
	{
		System.loadLibrary("tensorflow_inference");
	}

	protected float[] forward(Stream[] stream)
	{
		if (stream.length != 1)
		{
			Log.w("only one input stream currently supported, consider using merge");
			return null;
		}
		if (!_isTrained)
		{
			Log.w("not trained");
			return null;
		}
		if (stream[0].type != Cons.Type.FLOAT) {
			Log.w ("invalid stream type");
			return null;
		}

		float[] ptr = stream[0].ptrF();

		FloatBuffer fb = FloatBuffer.allocate(stream[0].dim);

		for (float f : ptr)
			fb.put(f);
		fb.rewind();

		Tensor inputTensor = Tensor.create(new long[] {stream[0].num, stream[0].dim}, fb);
		Tensor resultTensor = session.runner()
				.feed("input/x", inputTensor)
				.fetch("Wx_plus_b/output")
				.run().get(0);

		float[][] probabilities = new float[1][classNum];
		resultTensor.copyTo(probabilities);

		return probabilities[0];
	}

	@Override
	void train(Stream[] stream)
	{
		Log.e("training not supported yet");
	}

	protected void loadOption(File file)
	{
		// Empty implementation
	}

	@Override
	void save(File file)
	{
		Log.e("saving not supported yet");
	}

	@Override
	int getNumClasses()
	{
		return classNum;
	}

	public void setNumClasses(int classNum)
	{
		this.classNum = classNum;
	}

	@Override
	String[] getClassNames()
	{
		return classNames;
	}

	public void setClassNames(String[] classNames)
	{
		this.classNames = classNames;
	}

	protected void load(File file)
	{
		FileInputStream fileInputStream;
		byte[] fileBytes = new byte[(int) file.length()];

		try
		{
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(fileBytes);
			fileInputStream.close();

			modelGraph = new Graph();
			modelGraph.importGraphDef(fileBytes);
			session = new Session(modelGraph);
		}
		catch (Exception e)
		{
			Log.e("Error while importing the model: " + e.getMessage());
			return;
		}

		_isTrained = true;
	}
}
