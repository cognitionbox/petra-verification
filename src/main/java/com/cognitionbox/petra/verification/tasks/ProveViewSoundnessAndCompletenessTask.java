package com.cognitionbox.petra.verification.tasks;

import com.cognitionbox.petra.verification.CompilationUnitWithData;

public class ProveViewSoundnessAndCompletenessTask extends BaseVerificationTask {
        public ProveViewSoundnessAndCompletenessTask(CompilationUnitWithData view) {
            this.view = view;
        }

        public CompilationUnitWithData getView() {
            return view;
        }

        private final CompilationUnitWithData view;

        @Override
        public String toString() {
            return "View:"+view.getClazz().getSimpleName();
        }
    }