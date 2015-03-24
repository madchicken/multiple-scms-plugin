package jenkins.scm.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.model.TopLevelItemDescriptor;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import jenkins.scm.api.*;
import org.kohsuke.stapler.AncestorInPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiSCM extends SCMSource {
    private List<SingleSCMSource> sources = new ArrayList<SingleSCMSource>();

    public MultiSCM(String id) {
        super(id);
    }

    @NonNull
    @Override
    protected void retrieve(SCMHeadObserver scmHeadObserver, TaskListener taskListener) throws IOException, InterruptedException {
        for(SingleSCMSource source : sources) {
            source.retrieve(scmHeadObserver, taskListener);
        }
    }

    @NonNull
    @Override
    public SCM build(SCMHead scmHead, SCMRevision scmRevision) {
        for(SingleSCMSource source : sources) {
            source.build(scmHead, scmRevision);
        }
        return null;
    }

    public List<SingleSCMSource> getSources() {
        return sources;
    }

    public void setSources(List<SingleSCMSource> sources) {
        this.sources = sources;
    }

    /**
     * Our descriptor.
     */
    @Extension
    @SuppressWarnings("unused") // instantiated by Jenkins
    public static class DescriptorImpl extends SCMSourceDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.SingleSCMSource_DisplayName();
        }

        /**
         * Returns the {@link SCMDescriptor} instances that are appropriate for the current context.
         *
         * @param context the current context.
         * @return the {@link SCMDescriptor} instances
         */
        @SuppressWarnings("unused") // used by stapler binding
        public static List<SCMDescriptor<?>> getSCMDescriptors(@AncestorInPath SCMSourceOwner context) {
            List<SCMDescriptor<?>> result = new ArrayList<SCMDescriptor<?>>(SCM.all());
            for (Iterator<SCMDescriptor<?>> iterator = result.iterator(); iterator.hasNext(); ) {
                SCMDescriptor<?> d = iterator.next();
                if (NullSCM.class.equals(d.clazz)) {
                    iterator.remove();
                }
            }
            if (context != null && context instanceof Describable) {
                final Descriptor descriptor = ((Describable) context).getDescriptor();
                if (descriptor instanceof TopLevelItemDescriptor) {
                    final TopLevelItemDescriptor topLevelItemDescriptor = (TopLevelItemDescriptor) descriptor;
                    for (Iterator<SCMDescriptor<?>> iterator = result.iterator(); iterator.hasNext(); ) {
                        SCMDescriptor<?> d = iterator.next();
                        if (!topLevelItemDescriptor.isApplicable(d)) {
                            iterator.remove();
                        }
                    }
                }
            }
            return result;
        }
    }}

