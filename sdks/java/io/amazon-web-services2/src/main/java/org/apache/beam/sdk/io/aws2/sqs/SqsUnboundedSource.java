/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.io.aws2.sqs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.io.aws2.sqs.SqsIO.Read;
import org.apache.beam.sdk.options.PipelineOptions;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings({
  "nullness" // TODO(https://issues.apache.org/jira/browse/BEAM-10402)
})
class SqsUnboundedSource extends UnboundedSource<SqsMessage, SqsCheckpointMark> {
  private final Read read;

  public SqsUnboundedSource(Read read) {
    this.read = read;
  }

  @Override
  public List<SqsUnboundedSource> split(int desiredNumSplits, PipelineOptions options) {
    List<SqsUnboundedSource> sources = new ArrayList<>();
    for (int i = 0; i < Math.max(1, desiredNumSplits); ++i) {
      sources.add(new SqsUnboundedSource(read));
    }
    return sources;
  }

  @Override
  public UnboundedReader<SqsMessage> createReader(
      PipelineOptions options, @Nullable SqsCheckpointMark checkpointMark) {
    try {
      return new SqsUnboundedReader(this, checkpointMark);
    } catch (IOException e) {
      throw new RuntimeException("Unable to subscribe to " + read.queueUrl() + ": ", e);
    }
  }

  @Override
  public Coder<SqsCheckpointMark> getCheckpointMarkCoder() {
    return SerializableCoder.of(SqsCheckpointMark.class);
  }

  @Override
  public Coder<SqsMessage> getOutputCoder() {
    return SerializableCoder.of(SqsMessage.class);
  }

  public Read getRead() {
    return read;
  }

  @Override
  public boolean requiresDeduping() {
    return true;
  }
}
