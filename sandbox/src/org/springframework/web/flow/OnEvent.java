/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

/**
 * Static factory for creating Transitions that fire on occurences of specific
 * events.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class OnEvent {

    /**
     * Factory for transitions applicable to the "success" event.
     */
    public static class Success {
        public static Transition view(String viewStatePrefix) {
            return success(Flow.buildStateId(Flow.VIEW_SUFFIX, viewStatePrefix));
        }

        public static Transition get(String getStatePrefix) {
            return success(Flow.buildStateId(Flow.GET_ACTION_SUFFIX, getStatePrefix));
        }

        public static Transition edit(String getStatePrefix) {
            return success(Flow.buildStateId(Flow.EDIT_SUFFIX, getStatePrefix));
        }

        public static Transition populate(String getStatePrefix) {
            return success(Flow.buildStateId(Flow.POPULATE_FORM_ACTION_SUFFIX, getStatePrefix));
        }

        public static Transition add(String addStatePrefix) {
            return success(Flow.buildStateId(Flow.ADD_ACTION_SUFFIX, addStatePrefix));
        }

        public static Transition remove(String removeStatePrefix) {
            return success(Flow.buildStateId(Flow.REMOVE_ACTION_SUFFIX, removeStatePrefix));
        }

        public static Transition delete(String deleteStatePrefix) {
            return success(Flow.buildStateId(Flow.DELETE_ACTION_SUFFIX, deleteStatePrefix));
        }

        public static Transition save(String getStatePrefix) {
            return success(Flow.buildStateId(Flow.SAVE_ACTION_SUFFIX, getStatePrefix));
        }

        public static Transition end() {
            return success(EndState.DEFAULT_FINISH_STATE_ID);
        }
    }

    /**
     * Factory for transitions applicable to the "error" event.
     */
    public static class Error {
        public static Transition view(String viewStatePrefix) {
            return error(Flow.buildStateId(Flow.VIEW_SUFFIX, viewStatePrefix));
        }
    }

    /**
     * Factory for transitions applicable to the "submit" event.
     */
    public static class Submit {
        public static Transition bindAndValidate(String statePrefix) {
            return submit(Flow.buildStateId(Flow.BIND_AND_VALIDATE_FORM_ACTION_SUFFIX, statePrefix));
        }

        public static Transition end() {
            return submit(EndState.DEFAULT_FINISH_STATE_ID);
        }
    }

    /**
     * Factory for transitions commonly applicable to the "back" event.
     */
    public static class Back {
        public static Transition cancel() {
            return back(EndState.DEFAULT_CANCEL_STATE_ID);
        }

        public static Transition populate(String statePrefix) {
            return back(Flow.populate(statePrefix));
        }
        
        public static Transition view(String statePrefix) {
            return back(Flow.view(statePrefix));
        }

        public static Transition edit(String statePrefix) {
            return back(Flow.edit(statePrefix));
        }

        public static Transition end() {
            return back();
        }
    }

    /**
     * Factory for transitions commonly applicable to the "cancel" event.
     */
    public static class Cancel {
        public static Transition end() {
            return cancel();
        }
    }

    /**
     * Factory for transitions commonly applicable to the "finish" event.
     */
    public static class Finish {
        
        public static Transition get(String statePrefix) {
            return finish(Flow.get(statePrefix));
        }
        
        public static Transition populate(String statePrefix) {
            return finish(Flow.populate(statePrefix));
        }

        public static Transition save(String statePrefix) {
            return finish(Flow.save(statePrefix));
        }

        public static Transition edit(String statePrefix) {
            return finish(Flow.edit(statePrefix));
        }

        public static Transition end() {
            return finish();
        }
    }

    public static Transition get(String getStateId) {
        return new Transition("get", getStateId);
    }
    
    public static Transition view(String viewStateId) {
        return new Transition("view", viewStateId);
    }
    
    public static Transition submit(String submitStateId) {
        return new Transition("submit", submitStateId);
    }

    public static Transition bindAndValidate(String bindAndValidateStateId) {
        return new Transition("bindAndValidate", bindAndValidateStateId);
    }

    public static Transition success(String successStateId) {
        return new Transition("success", successStateId);
    }

    public static Transition edit(String editStateId) {
        return new Transition("edit", editStateId);
    }

    public static Transition add(String addStateId) {
        return new Transition("add", addStateId);
    }

    public static Transition remove(String removeStateId) {
        return new Transition("remove", removeStateId);
    }

    public static Transition link(String linkStateId) {
        return new Transition("link", linkStateId);
    }

    public static Transition unlink(String unlinkStateId) {
        return new Transition("unlink", unlinkStateId);
    }

    public static Transition search(String searchStateId) {
        return new Transition("search", searchStateId);
    }

    public static Transition error(String errorStateId) {
        return new Transition("error", errorStateId);
    }

    public static Transition next(String nextStateId) {
        return new Transition("next", nextStateId);
    }

    public static Transition back(String backStateId) {
        return new Transition("back", backStateId);
    }

    public static Transition back() {
        return back(EndState.DEFAULT_BACK_STATE_ID);
    }

    public static Transition cancel(String endCancelStateId) {
        return new Transition("cancel", endCancelStateId);
    }

    public static Transition finish(String endOkStateId) {
        return new Transition("finish", endOkStateId);
    }

    public static Transition cancel() {
        return cancel(EndState.DEFAULT_CANCEL_STATE_ID);
    }

    public static Transition finish() {
        return finish(EndState.DEFAULT_FINISH_STATE_ID);
    }
}
