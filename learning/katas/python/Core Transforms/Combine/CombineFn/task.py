#   Licensed to the Apache Software Foundation (ASF) under one
#   or more contributor license agreements.  See the NOTICE file
#   distributed with this work for additional information
#   regarding copyright ownership.  The ASF licenses this file
#   to you under the Apache License, Version 2.0 (the
#   "License"); you may not use this file except in compliance
#   with the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

# beam-playground:
#   name: CombineFn
#   description: Task from katas averaging.
#   multifile: false
#   pipeline_options:
#   categories:
#     - Combiners

import apache_beam as beam

from log_elements import LogElements


class AverageFn(beam.CombineFn):

    def create_accumulator(self):
        return 0.0, 0

    def add_input(self, accumulator, element):
        (sum, count) = accumulator
        return sum + element, count + 1

    def merge_accumulators(self, accumulators):
        sums, counts = zip(*accumulators)
        return sum(sums), sum(counts)

    def extract_output(self, accumulator):
        (sum, count) = accumulator
        return sum / count if count else float('NaN')


with beam.Pipeline() as p:

  (p | beam.Create([10, 20, 50, 70, 90])
     | beam.CombineGlobally(AverageFn())
     | LogElements())

