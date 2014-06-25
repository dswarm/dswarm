package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Job;
import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Job}s.
 *
 * @author tgaengler
 */
@XmlRootElement
public class ProxyJob extends ProxyExtendedBasicDMPJPAObject<Job> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created job, i.e., no updated or already existing job.
	 *
	 * @param jobArg a freshly created job
	 */
	public ProxyJob(final Job jobArg) {

		super(jobArg);
	}

	/**
	 * Creates a new proxy with the given real job and the type how the job was processed by the job persistence service, e.g.,
	 * {@link RetrievalType.CREATED}.
	 *
	 * @param jobArg a job that was processed by the job persistence service
	 * @param typeArg the type how this job was processed by the job persistence service
	 */
	public ProxyJob(final Job jobArg, final RetrievalType typeArg) {

		super(jobArg, typeArg);
	}
}
